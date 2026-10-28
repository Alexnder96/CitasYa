<?php
$servername = "localhost";
$username = "root";
$password = "";
$database = "citas_medicas";
$port = 3307;

$conn = new mysqli($servername, $username, $password, $database, $port);

if ($conn->connect_error) {
    die("Conexión fallida: " . $conn->connect_error);
}

$conn->select_db("citas_medicas");
?>
