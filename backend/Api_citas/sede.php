<?php
include 'db_connection.php';

// Manejar las solicitudes HTTP
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Por defecto solo trae sedes activas; ?estado=0 trae las inactivas
    if (isset($_GET['estado'])) {
        $estado = (int) $_GET['estado'];
        $stmt = $conn->prepare("SELECT * FROM SEDE WHERE estado = ?");
        $stmt->bind_param("i", $estado);
        $stmt->execute();
        $result = $stmt->get_result();
    } else {
        $result = $conn->query("SELECT * FROM SEDE WHERE estado = 1");
    }

    if ($result->num_rows > 0) {
        $sedes = array();

        while ($row = $result->fetch_assoc()) {
            $sedes[] = $row;
        }

        echo json_encode($sedes);
    } else {
        echo json_encode(array('message' => 'No se encontraron sedes.'));
    }

} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Crear una nueva sede (queda activa por defecto)
    $data = json_decode(file_get_contents("php://input"), true);

    $nombre = $data['nombre'];
    $direccion = isset($data['direccion']) ? $data['direccion'] : null;
    $telefono = isset($data['telefono']) ? $data['telefono'] : null;

    $querySede = "INSERT INTO SEDE (nombre, direccion, telefono, estado)
                  VALUES (?, ?, ?, 1)";

    $stmt = $conn->prepare($querySede);
    $stmt->bind_param("sss", $nombre, $direccion, $telefono);

    if ($stmt->execute()) {
        echo json_encode(array('message' => 'Sede creada exitosamente.'));
    } else {
        echo json_encode(array('error' => $stmt->error));
    }

    $stmt->close();

} elseif ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    // Actualizar información de la sede (nombre/dirección/teléfono y/o estado)
    $data = json_decode(file_get_contents("php://input"), true);

    $idsede = isset($data['idsede']) ? $data['idsede'] : null;
    $nombre = isset($data['nombre']) ? $data['nombre'] : null;
    $direccion = isset($data['direccion']) ? $data['direccion'] : null;
    $telefono = isset($data['telefono']) ? $data['telefono'] : null;
    $estado = isset($data['estado']) ? $data['estado'] : null;

    if ($idsede !== null) {
        $query = "UPDATE SEDE
                  SET nombre = COALESCE(?, nombre),
                      direccion = COALESCE(?, direccion),
                      telefono = COALESCE(?, telefono),
                      estado = COALESCE(?, estado)
                  WHERE idsede=?";

        $stmt = $conn->prepare($query);
        $stmt->bind_param("sssii", $nombre, $direccion, $telefono, $estado, $idsede);

        if ($stmt->execute()) {
            echo json_encode(array('message' => 'Información de la sede actualizada exitosamente.'));
        } else {
            echo json_encode(array('error' => $stmt->error));
        }

        $stmt->close();
    } else {
        echo json_encode(array('error' => 'El idsede no está presente en la solicitud.'));
    }

} elseif ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    // Baja lógica: ya no se borra la sede, se marca como inactiva (estado = 0)
    $idsede = $_GET['idsede'];

    $query = "UPDATE SEDE SET estado = 0 WHERE idsede=?";

    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $idsede);

    if ($stmt->execute()) {
        echo json_encode(array('message' => 'Sede desactivada exitosamente.'));
    } else {
        echo json_encode(array('error' => $stmt->error));
    }

    $stmt->close();
}

$conn->close();
?>
