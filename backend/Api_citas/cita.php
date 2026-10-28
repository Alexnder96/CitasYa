<?php
include 'db_connection.php';

// Manejar las solicitudes HTTP
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $idpaciente = isset($_GET['idpaciente']) ? $_GET['idpaciente'] : null;

    if ($idpaciente !== null) {
        // Las citas cuya fecha ya pasó (antes de hoy) y seguían en 'confirmada' o 'pendiente'
        // se marcan automáticamente como 'atendida' antes de listarlas -- así no queda una cita
        // de la semana pasada mostrando "CONFIRMADA" para siempre.
        $queryActualizarAtendidas = "UPDATE CITA
                                      SET estado = 'atendida'
                                      WHERE idpaciente = ?
                                        AND estado IN ('confirmada', 'pendiente')
                                        AND fecha < CURDATE()";
        $stmtAtendidas = $conn->prepare($queryActualizarAtendidas);
        $stmtAtendidas->bind_param("i", $idpaciente);
        $stmtAtendidas->execute();
        $stmtAtendidas->close();

        // Obtener todas las citas de un paciente específico con el nombre del doctor y el nombre de la especialidad
        $query = "SELECT CITA.*, DOCTOR.nombre AS nombre_doctor, ESPECIALIDAD.nombreespecialidad, SEDE.nombre AS nombre_sede
                  FROM CITA
                  JOIN DOCTOR ON CITA.iddoctor = DOCTOR.iddoctor
                  JOIN ESPECIALIDAD ON CITA.especialidad = ESPECIALIDAD.idespecialidad
                  LEFT JOIN SEDE ON CITA.idsede = SEDE.idsede
                  WHERE idpaciente = ?
                  ORDER BY CITA.fecha DESC";

        $stmt = $conn->prepare($query);
        $stmt->bind_param("i", $idpaciente);

        $stmt->execute();
        $result = $stmt->get_result();
        $citas = array();

        while ($row = $result->fetch_assoc()) {
            $citas[] = $row;
        }

        echo json_encode($citas);

        $stmt->close();

    } elseif (isset($_GET['iddoctor']) && isset($_GET['desde']) && isset($_GET['hasta'])) {
        // Horarios ya ocupados de un doctor en un rango de fechas (para deshabilitarlos en la app).
        // Las citas "cancelada" no cuentan: ese horario vuelve a estar libre.
        $iddoctor = $_GET['iddoctor'];
        $desde = $_GET['desde'];
        $hasta = $_GET['hasta'];

        $query = "SELECT fecha, hora
                  FROM CITA
                  WHERE iddoctor = ? AND fecha BETWEEN ? AND ? AND estado <> 'cancelada'";

        $stmt = $conn->prepare($query);
        $stmt->bind_param("iss", $iddoctor, $desde, $hasta);
        $stmt->execute();
        $result = $stmt->get_result();
        $ocupados = array();

        while ($row = $result->fetch_assoc()) {
            $ocupados[] = $row;
        }

        echo json_encode($ocupados);

        $stmt->close();

    } else {
        echo json_encode(array('error' => 'Se requiere el parámetro idpaciente, o iddoctor + desde + hasta, para obtener las citas.'));
    }

} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Crear una nueva cita (queda "confirmada" al reservarla)
    $data = json_decode(file_get_contents("php://input"), true);

    $fecha = $data['fecha'];
    $hora = $data['hora'];
    $especialidad = $data['especialidad'];
    $idpaciente = $data['idpaciente'];
    $iddoctor = $data['iddoctor'];
    $idsede = isset($data['idsede']) ? (int) $data['idsede'] : null;

    // Chequeo previo: ¿ese doctor ya tiene una cita activa en esa fecha y hora?
    // (No evita del todo una carrera entre dos reservas simultáneas -- para eso
    // está la llave única slot_ocupado en la base -- pero da un mensaje claro
    // en el caso normal, sin depender solo del error de MySQL.)
    $stmtCheck = $conn->prepare("SELECT idcita FROM CITA WHERE iddoctor = ? AND fecha = ? AND hora = ? AND estado <> 'cancelada'");
    $stmtCheck->bind_param("iss", $iddoctor, $fecha, $hora);
    $stmtCheck->execute();
    $yaOcupado = $stmtCheck->get_result()->num_rows > 0;
    $stmtCheck->close();

    if ($yaOcupado) {
        http_response_code(409);
        echo json_encode(array('error' => 'Ese horario ya fue tomado por otro paciente. Elige otro.'));
        exit;
    }

    $queryCita = "INSERT INTO CITA (fecha, hora, especialidad, idpaciente, iddoctor, idsede, estado)
                  VALUES (?, ?, ?, ?, ?, ?, 'confirmada')";

    $stmt = $conn->prepare($queryCita);
    $stmt->bind_param("ssiiii", $fecha, $hora, $especialidad, $idpaciente, $iddoctor, $idsede);

    try {
        $stmt->execute();
        echo json_encode(array('message' => 'Cita creada exitosamente.'));
    } catch (mysqli_sql_exception $e) {
        if ($e->getCode() == 1062) {
            // Llave única slot_ocupado: ese horario ya fue tomado
            http_response_code(409);
            echo json_encode(array('error' => 'Ese horario ya fue tomado por otro paciente. Elige otro.'));
        } else {
            http_response_code(500);
            echo json_encode(array('error' => $e->getMessage()));
        }
    }

    $stmt->close();

} elseif ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    // Actualizar información de la cita, o solo su estado
    $data = json_decode(file_get_contents("php://input"), true);

    $idcita = isset($data['idcita']) ? $data['idcita'] : null;
    $fecha = isset($data['fecha']) ? $data['fecha'] : null;
    $hora = isset($data['hora']) ? $data['hora'] : null;
    $especialidad = isset($data['especialidad']) ? $data['especialidad'] : null;
    $idpaciente = isset($data['idpaciente']) ? $data['idpaciente'] : null;
    $iddoctor = isset($data['iddoctor']) ? $data['iddoctor'] : null;
    $estado = isset($data['estado']) ? $data['estado'] : null;

    if ($idcita === null) {
        echo json_encode(array('error' => 'El idcita no está presente en la solicitud.'));
        exit;
    }

    if ($fecha === null && $hora === null && $especialidad === null && $idpaciente === null && $iddoctor === null) {
        // Solo llegó el estado: cambiar únicamente pendiente/confirmada/cancelada/atendida
        $validos = array('pendiente', 'confirmada', 'cancelada', 'atendida');
        if ($estado === null || !in_array($estado, $validos, true)) {
            echo json_encode(array('error' => 'Estado inválido.'));
            exit;
        }

        $query = "UPDATE CITA SET estado=? WHERE idcita=?";
        $stmt = $conn->prepare($query);
        $stmt->bind_param("si", $estado, $idcita);
    } else {
        // Actualización completa de la cita (el estado es opcional; si no llega, se conserva el actual)
        $query = "UPDATE CITA
                  SET fecha=?, hora=?, especialidad=?, idpaciente=?, iddoctor=?,
                      estado = COALESCE(?, estado)
                  WHERE idcita=?";

        $stmt = $conn->prepare($query);
        $stmt->bind_param("ssiiisi", $fecha, $hora, $especialidad, $idpaciente, $iddoctor, $estado, $idcita);
    }

    if ($stmt->execute()) {
        echo json_encode(array('message' => 'Información de la cita actualizada exitosamente.'));
    } else if ($conn->errno == 1062) {
        http_response_code(409);
        echo json_encode(array('error' => 'Ese horario ya fue tomado por otro paciente. Elige otro.'));
    } else {
        echo json_encode(array('error' => $stmt->error));
    }

    $stmt->close();

} elseif ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    // Ya no se borra la cita: se marca como "cancelada" (misma idea de baja lógica que en paciente/doctor)
    $idcita = $_GET['idcita'];

    $query = "UPDATE CITA SET estado = 'cancelada' WHERE idcita=?";

    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $idcita);

    if ($stmt->execute()) {
        echo json_encode(array('message' => 'Cita cancelada exitosamente.'));
    } else {
        echo json_encode(array('error' => $stmt->error));
    }

    $stmt->close();
}

$conn->close();
?>
