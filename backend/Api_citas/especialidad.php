<?php
include 'db_connection.php';

// Manejar las solicitudes HTTP
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Obtener todas las especialidades
    $query = "SELECT * FROM ESPECIALIDAD";
    $result = $conn->query($query);

    if ($result->num_rows > 0) {
        $especialidades = array();

        while ($row = $result->fetch_assoc()) {
            $especialidades[] = $row;
        }

        echo json_encode($especialidades);
    } else {
        echo json_encode(array('message' => 'No se encontraron especialidades.'));
    }

} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Crear una nueva especialidad
    $data = json_decode(file_get_contents("php://input"), true);

    $nombreespecialidad = $data['nombreespecialidad'];
    $descripcion = $data['descripcion'];

    $queryEspecialidad = "INSERT INTO ESPECIALIDAD (nombreespecialidad, descripcion)
                          VALUES (?, ?)";

    $stmt = $conn->prepare($queryEspecialidad);
    $stmt->bind_param("ss", $nombreespecialidad, $descripcion);

    if ($stmt->execute()) {
        echo json_encode(array('message' => 'Especialidad creada exitosamente.'));
    } else {
        echo json_encode(array('error' => $stmt->error));
    }

    $stmt->close();

} elseif ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    // Actualizar información de la especialidad
    $data = json_decode(file_get_contents("php://input"), true);

    $idespecialidad = isset($data['idespecialidad']) ? $data['idespecialidad'] : null;
    $nombreespecialidad = isset($data['nombreespecialidad']) ? $data['nombreespecialidad'] : null;
    $descripcion = isset($data['descripcion']) ? $data['descripcion'] : null;

    if ($idespecialidad !== null) {
        $query = "UPDATE ESPECIALIDAD
                  SET nombreespecialidad=?, descripcion=?
                  WHERE idespecialidad=?";

        $stmt = $conn->prepare($query);
        $stmt->bind_param("ssi", $nombreespecialidad, $descripcion, $idespecialidad);

        if ($stmt->execute()) {
            echo json_encode(array('message' => 'Información de la especialidad actualizada exitosamente.'));
        } else {
            echo json_encode(array('error' => $stmt->error));
        }

        $stmt->close();
    } else {
        echo json_encode(array('error' => 'El idespecialidad no está presente en la solicitud.'));
    }

} elseif ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    // Eliminar una especialidad
    $idespecialidad = $_GET['idespecialidad'];

    $query = "DELETE FROM ESPECIALIDAD WHERE idespecialidad=?";

    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $idespecialidad);

    if ($stmt->execute()) {
        echo json_encode(array('message' => 'Especialidad eliminada exitosamente.'));
    } else {
        echo json_encode(array('error' => $stmt->error));
    }

    $stmt->close();
}

$conn->close();
?>
