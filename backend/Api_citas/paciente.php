<?php
include 'db_connection.php';

// Calcula la edad actual a partir de la fecha de nacimiento (viene de PACIENTE_HOSPITAL)
function calcularEdad($fechaNacimiento) {
    if (!$fechaNacimiento) {
        return null;
    }
    $hoy = new DateTime();
    $nacimiento = new DateTime($fechaNacimiento);
    return $hoy->diff($nacimiento)->y;
}

// Manejar las solicitudes HTTP
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Los datos personales (nombre, sexo, teléfono, sede) ya no viven en PACIENTE:
    // se traen de PACIENTE_HOSPITAL uniendo por dni.
    if (isset($_GET['idpaciente'])) {
        $idpaciente = $_GET['idpaciente'];

        $query = "SELECT p.idpaciente, p.dni, p.correo, p.estado,
                         h.nombre, h.fecha_nacimiento, h.sexo, h.telefono, h.idsede,
                         s.nombre AS nombre_sede
                  FROM PACIENTE p
                  LEFT JOIN PACIENTE_HOSPITAL h ON p.dni = h.dni
                  LEFT JOIN SEDE s ON h.idsede = s.idsede
                  WHERE p.idpaciente = ?";

        $stmt = $conn->prepare($query);
        $stmt->bind_param("i", $idpaciente);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows > 0) {
            $paciente = $result->fetch_assoc();
            $paciente['edad'] = calcularEdad($paciente['fecha_nacimiento']);
            echo json_encode($paciente);
        } else {
            echo json_encode(array('message' => 'No se encontró el usuario.'));
        }
        $stmt->close();

    } elseif (isset($_GET['correo'])) {
        $correo = $_GET['correo'];

        $query = "SELECT p.idpaciente, p.dni, p.correo, p.estado,
                         h.nombre, h.fecha_nacimiento, h.sexo, h.telefono, h.idsede,
                         s.nombre AS nombre_sede
                  FROM PACIENTE p
                  LEFT JOIN PACIENTE_HOSPITAL h ON p.dni = h.dni
                  LEFT JOIN SEDE s ON h.idsede = s.idsede
                  WHERE p.correo = ?";

        $stmt = $conn->prepare($query);
        $stmt->bind_param("s", $correo);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows > 0) {
            $paciente = $result->fetch_assoc();
            $paciente['edad'] = calcularEdad($paciente['fecha_nacimiento']);
            echo json_encode($paciente);
        } else {
            echo json_encode(array('message' => 'No se encontró el usuario.'));
        }
        $stmt->close();

    } else {
        echo json_encode(array('error' => 'El parámetro de correo electrónico o ID de paciente es requerido.'));
    }

} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);

    // --- Login: POST con { "accion": "login", "correo": "...", "password": "..." } ---
    if (isset($data['accion']) && $data['accion'] === 'login') {
        $correo = $data['correo'];
        $passwordIngresado = $data['password'];

        $query = "SELECT p.idpaciente, p.dni, p.correo, p.password, p.estado,
                         h.nombre, h.fecha_nacimiento, h.sexo, h.telefono, h.idsede,
                         s.nombre AS nombre_sede
                  FROM PACIENTE p
                  LEFT JOIN PACIENTE_HOSPITAL h ON p.dni = h.dni
                  LEFT JOIN SEDE s ON h.idsede = s.idsede
                  WHERE p.correo = ?";
        $stmt = $conn->prepare($query);
        $stmt->bind_param("s", $correo);
        $stmt->execute();
        $result = $stmt->get_result();
        $paciente = $result->fetch_assoc();
        $stmt->close();

        if (!$paciente || !password_verify($passwordIngresado, $paciente['password'])) {
            http_response_code(401);
            echo json_encode(array('error' => 'Correo o contraseña incorrecta.'));
            exit;
        }

        if ((int) $paciente['estado'] !== 1) {
            http_response_code(403);
            echo json_encode(array('error' => 'Tu cuenta está inactiva.'));
            exit;
        }

        unset($paciente['password']); // nunca se devuelve el hash al cliente
        $paciente['edad'] = calcularEdad($paciente['fecha_nacimiento']);
        echo json_encode($paciente);
        exit;
    }

    // --- Recuperar contraseña: POST con { "accion": "recuperar_password", "dni": "...", "correo": "...", "nuevaPassword": "..." } ---
    // Verifica que el dni + correo coincidan con una cuenta existente antes de permitir el cambio.
    if (isset($data['accion']) && $data['accion'] === 'recuperar_password') {
        $dni = isset($data['dni']) ? trim($data['dni']) : null;
        $correo = isset($data['correo']) ? trim($data['correo']) : null;
        $nuevaPassword = isset($data['nuevaPassword']) ? $data['nuevaPassword'] : null;

        if (!$dni || !$correo || !$nuevaPassword) {
            http_response_code(400);
            echo json_encode(array('error' => 'Se requieren dni, correo y nuevaPassword.'));
            exit;
        }

        $stmt = $conn->prepare("SELECT idpaciente FROM PACIENTE WHERE dni = ? AND correo = ?");
        $stmt->bind_param("ss", $dni, $correo);
        $stmt->execute();
        $result = $stmt->get_result();
        $paciente = $result->fetch_assoc();
        $stmt->close();

        if (!$paciente) {
            http_response_code(404);
            echo json_encode(array('error' => 'No encontramos una cuenta con ese DNI y ese correo.'));
            exit;
        }

        $passwordHasheada = password_hash($nuevaPassword, PASSWORD_DEFAULT);

        $stmt = $conn->prepare("UPDATE PACIENTE SET password = ? WHERE idpaciente = ?");
        $stmt->bind_param("si", $passwordHasheada, $paciente['idpaciente']);

        if ($stmt->execute()) {
            echo json_encode(array('message' => 'Contraseña actualizada correctamente.'));
        } else {
            echo json_encode(array('error' => $stmt->error));
        }
        $stmt->close();
        exit;
    }

    // --- Registro: POST con { "dni": "...", "correo": "...", "password": "..." } ---
    // Ya no se piden nombre/edad/sexo/telefono/sede: se jalan de PACIENTE_HOSPITAL por dni.
    $dni = isset($data['dni']) ? trim($data['dni']) : null;
    $correo = isset($data['correo']) ? trim($data['correo']) : null;
    $passwordPlano = isset($data['password']) ? $data['password'] : null;

    if (!$dni || !$correo || !$passwordPlano) {
        http_response_code(400);
        echo json_encode(array('error' => 'Se requieren dni, correo y password.'));
        exit;
    }

    // 1) El DNI debe existir en el padrón del hospital
    $stmt = $conn->prepare("SELECT dni FROM PACIENTE_HOSPITAL WHERE dni = ?");
    $stmt->bind_param("s", $dni);
    $stmt->execute();
    $existeEnHospital = $stmt->get_result()->num_rows > 0;
    $stmt->close();

    if (!$existeEnHospital) {
        http_response_code(404);
        echo json_encode(array('error' => 'El DNI ingresado no está registrado en el hospital.'));
        exit;
    }

    // 2) Ese DNI no debe tener ya una cuenta creada
    $stmt = $conn->prepare("SELECT idpaciente FROM PACIENTE WHERE dni = ?");
    $stmt->bind_param("s", $dni);
    $stmt->execute();
    $yaTieneCuenta = $stmt->get_result()->num_rows > 0;
    $stmt->close();

    if ($yaTieneCuenta) {
        http_response_code(409);
        echo json_encode(array('error' => 'Ya existe una cuenta registrada con ese DNI.'));
        exit;
    }

    // 3) Ese correo tampoco debe estar en uso por otra cuenta
    $stmt = $conn->prepare("SELECT idpaciente FROM PACIENTE WHERE correo = ?");
    $stmt->bind_param("s", $correo);
    $stmt->execute();
    $correoEnUso = $stmt->get_result()->num_rows > 0;
    $stmt->close();

    if ($correoEnUso) {
        http_response_code(409);
        echo json_encode(array('error' => 'Ese correo ya se encuentra registrado.'));
        exit;
    }

    // 4) Crear la cuenta (password encriptado, estado activo por defecto)
    $password = password_hash($passwordPlano, PASSWORD_DEFAULT);

    $stmt = $conn->prepare("INSERT INTO PACIENTE (dni, correo, password, estado) VALUES (?, ?, ?, 1)");
    $stmt->bind_param("sss", $dni, $correo, $password);

    if ($stmt->execute()) {
        echo json_encode(array('message' => 'Paciente registrado exitosamente.'));
    } else {
        echo json_encode(array('error' => $stmt->error));
    }
    $stmt->close();

} elseif ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    // Actualizar contraseña y/o estado del paciente.
    // (La sede ya no se edita aquí: viene fija desde PACIENTE_HOSPITAL.)
    $data = json_decode(file_get_contents("php://input"), true);

    $idpaciente = isset($data['idpaciente']) ? $data['idpaciente'] : null;

    if ($idpaciente === null) {
        echo json_encode(array('error' => 'El idpaciente no está presente en la solicitud.'));
        exit;
    }

    if (isset($data['password'])) {
        $password = password_hash($data['password'], PASSWORD_DEFAULT);

        $query = "UPDATE PACIENTE SET password=? WHERE idpaciente=?";
        $stmt = $conn->prepare($query);
        $stmt->bind_param("si", $password, $idpaciente);

        if ($stmt->execute()) {
            echo json_encode(array('message' => 'Contraseña del paciente actualizada exitosamente.'));
        } else {
            echo json_encode(array('error' => $stmt->error));
        }
        $stmt->close();

    } elseif (isset($data['estado'])) {
        $estado = (int) $data['estado'];

        $query = "UPDATE PACIENTE SET estado=? WHERE idpaciente=?";
        $stmt = $conn->prepare($query);
        $stmt->bind_param("ii", $estado, $idpaciente);

        if ($stmt->execute()) {
            echo json_encode(array('message' => 'Estado del paciente actualizado exitosamente.'));
        } else {
            echo json_encode(array('error' => $stmt->error));
        }
        $stmt->close();

    } else {
        echo json_encode(array('error' => 'No se envió password ni estado para actualizar.'));
    }

} elseif ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    // Baja lógica: se marca como inactivo (estado = 0)
    $idpaciente = $_GET['idpaciente'];

    $query = "UPDATE PACIENTE SET estado = 0 WHERE idpaciente=?";

    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $idpaciente);

    if ($stmt->execute()) {
        echo json_encode(array('message' => 'Paciente desactivado exitosamente.'));
    } else {
        echo json_encode(array('error' => $stmt->error));
    }

    $stmt->close();
}

$conn->close();
?>
