<?php
include 'db_connection.php';

// Manejar las solicitudes HTTP
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Obtener todos los doctores
    $query = "SELECT * FROM DOCTOR";
    $result = $conn->query($query);

    if ($result->num_rows > 0) {
        $doctores = array();

        while ($row = $result->fetch_assoc()) {
            $doctores[] = $row;
        }

        echo json_encode($doctores);
    } else {
        echo json_encode(array('message' => 'No se encontraron doctores.'));
    }

} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Crear un nuevo doctor (queda activo por defecto)
    $data = json_decode(file_get_contents("php://input"), true);

    $nombre = $data['nombre'];
    $idespecialidad = $data['idespecialidad'];
    $telefono = $data['telefono'];
    $estado = isset($data['estado']) ? (int) $data['estado'] : 1;

    $queryDoctor = "INSERT INTO DOCTOR (nombre, idespecialidad, telefono, estado)
                    VALUES (?, ?, ?, ?)";

    $stmt = $conn->prepare($queryDoctor);
    $stmt->bind_param("siii", $nombre, $idespecialidad, $telefono, $estado);

    if ($stmt->execute()) {
        echo json_encode(array('message' => 'Doctor creado exitosamente.'));
    } else {
        echo json_encode(array('error' => $stmt->error));
    }

    $stmt->close();

} elseif ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    // Actualizar información del doctor (nombre/especialidad/teléfono y/o estado)
    $data = json_decode(file_get_contents("php://input"), true);

    $iddoctor = isset($data['iddoctor']) ? $data['iddoctor'] : null;
    $nombre = isset($data['nombre']) ? $data['nombre'] : null;
    $idespecialidad = isset($data['idespecialidad']) ? $data['idespecialidad'] : null;
    $telefono = isset($data['telefono']) ? $data['telefono'] : null;
    $estado = isset($data['estado']) ? $data['estado'] : null;

    if ($iddoctor !== null) {
        $query = "UPDATE DOCTOR
                  SET nombre = COALESCE(?, nombre),
                      idespecialidad = COALESCE(?, idespecialidad),
                      telefono = COALESCE(?, telefono),
                      estado = COALESCE(?, estado)
                  WHERE iddoctor=?";

        $stmt = $conn->prepare($query);
        $stmt->bind_param("siisi", $nombre, $idespecialidad, $telefono, $estado, $iddoctor);

        if ($stmt->execute()) {
            echo json_encode(array('message' => 'Información del doctor actualizada exitosamente.'));
        } else {
            echo json_encode(array('error' => $stmt->error));
        }

        $stmt->close();
    } else {
        echo json_encode(array('error' => 'El iddoctor no está presente en la solicitud.'));
    }

} elseif ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    // Baja lógica: ya no se borra al doctor, se marca como inactivo (estado = 0)
    $iddoctor = $_GET['iddoctor'];

    $query = "UPDATE DOCTOR SET estado = 0 WHERE iddoctor=?";

    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $iddoctor);

    if ($stmt->execute()) {
        echo json_encode(array('message' => 'Doctor desactivado exitosamente.'));
    } else {
        echo json_encode(array('error' => $stmt->error));
    }

    $stmt->close();
}

$conn->close();
?>
