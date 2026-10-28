-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1:3307
-- Tiempo de generación: 28-10-2026 a las 05:16:15
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
-- =====================================================================
-- CitasYa - Script de base de datos `citas_medicas`  (MySQL / MariaDB 10.4)
-- Datos de PRUEBA ficticios: no contiene correos, contraseñas ni nombres reales.
-- Cuentas de prueba (tabla paciente): paciente01@example.com ... paciente10@example.com
-- Contraseña de todas las cuentas de prueba: Prueba123
-- DNI libres para probar el registro: 70000011 a 70000099 (existen en paciente_hospital)
-- Uso: importar desde phpMyAdmin (pestaña Importar) o con: mysql -u root -p < citas_medicas.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `citas_medicas` DEFAULT CHARACTER SET utf8 COLLATE utf8_spanish_ci;
USE `citas_medicas`;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cita`
--

CREATE TABLE `cita` (
  `idcita` int(11) NOT NULL,
  `fecha` date DEFAULT NULL,
  `hora` time DEFAULT NULL,
  `especialidad` int(11) DEFAULT NULL,
  `idpaciente` int(11) DEFAULT NULL,
  `iddoctor` int(11) DEFAULT NULL,
  `idsede` int(11) NOT NULL DEFAULT 1 COMMENT 'Sede donde se atendió/atenderá la cita',
  `estado` enum('pendiente','confirmada','cancelada','atendida') NOT NULL DEFAULT 'pendiente',
  `slot_ocupado` varchar(50) GENERATED ALWAYS AS (case when `estado` <> 'cancelada' then concat(`iddoctor`,'_',`fecha`,'_',`hora`) else NULL end) VIRTUAL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;

--
-- Volcado de datos para la tabla `cita`
--

INSERT INTO `cita` (`idcita`, `fecha`, `hora`, `especialidad`, `idpaciente`, `iddoctor`, `idsede`, `estado`) VALUES
(1, '2025-11-15', '10:00:00', 1, 1, 1, 1, 'atendida'),
(2, '2025-11-16', '11:30:00', 2, 1, 2, 1, 'atendida'),
(3, '2025-11-17', '14:15:00', 3, 1, 3, 1, 'atendida'),
(4, '2025-11-18', '16:45:00', 4, 1, 4, 1, 'atendida'),
(5, '2025-11-19', '09:30:00', 5, 1, 5, 1, 'atendida'),
(8, '2025-11-15', '10:30:00', 1, 1, 1, 1, 'atendida'),
(9, '2025-11-15', '10:00:00', 1, 2, 1, 1, 'cancelada'),
(10, '2025-11-15', '10:00:00', 1, 2, 1, 1, 'cancelada'),
(11, '2025-12-04', '09:35:00', 1, 2, 1, 1, 'confirmada'),
(12, '2025-12-04', '10:15:00', 3, 1, 1, 1, 'atendida'),
(15, '2025-12-14', '01:31:00', 5, 1, 1, 1, 'atendida'),
(16, '2025-12-15', '09:19:00', 1, 1, 1, 1, 'atendida'),
(17, '2025-11-15', '10:00:00', 1, 2, 1, 1, 'cancelada'),
(18, '2025-12-15', '09:15:00', 1, 1, 1, 1, 'atendida'),
(20, '2025-11-14', '02:51:00', 1, 1, 1, 1, 'atendida'),
(21, '2025-11-14', '02:52:00', 1, 1, 1, 1, 'atendida'),
(25, '2025-12-17', '05:40:00', 1, 1, 1, 1, 'atendida'),
(27, '2025-12-19', '03:20:00', 3, 1, 5, 1, 'atendida'),
(28, '2025-12-19', '03:50:00', 5, 1, 11, 1, 'atendida'),
(30, '2025-12-20', '05:09:00', 1, 3, 1, 1, 'confirmada'),
(31, '2025-12-29', '04:53:00', 3, 1, 6, 1, 'atendida'),
(32, '2026-03-08', '10:57:00', 1, 1, 1, 1, 'atendida'),
(33, '2026-03-22', '04:32:00', 1, 1, 1, 1, 'atendida'),
(35, '2025-11-15', '10:00:00', 1, 1, 1, 1, 'cancelada'),
(36, '2025-11-16', '11:30:00', 2, 1, 2, 1, 'cancelada'),
(37, '2025-11-17', '14:15:00', 3, 1, 1, 1, 'atendida'),
(38, '2026-04-08', '10:00:00', 1, 1, 1, 1, 'atendida'),
(39, '2026-04-09', '11:30:00', 1, 1, 2, 1, 'atendida'),
(40, '2026-04-09', '14:15:00', 1, 1, 1, 1, 'atendida'),
(41, '2026-04-09', '14:20:00', 1, 1, 1, 1, 'atendida'),
(46, '2026-04-09', '01:54:00', 1, 1, 1, 1, 'atendida'),
(47, '2026-04-09', '08:56:00', 3, 1, 6, 1, 'atendida'),
(48, '2026-04-15', '04:58:00', 1, 1, 1, 1, 'atendida'),
(49, '2026-04-14', '04:58:00', 1, 1, 1, 1, 'atendida'),
(50, '2026-04-14', '05:13:00', 2, 11, 3, 1, 'confirmada'),
(52, '2026-04-16', '04:09:00', 3, 1, 5, 1, 'atendida'),
(53, '2026-04-17', '03:53:00', 1, 1, 1, 1, 'atendida'),
(54, '2026-04-18', '02:46:00', 1, 1, 1, 1, 'atendida'),
(55, '2026-04-18', '03:53:00', 3, 13, 6, 1, 'confirmada'),
(56, '2026-04-18', '04:55:00', 1, 13, 1, 1, 'confirmada'),
(57, '2026-09-24', '03:48:00', 3, 1, 5, 1, 'atendida'),
(58, '2026-09-26', '03:34:00', 5, 1, 11, 1, 'cancelada'),
(59, '2026-09-24', '01:12:00', 5, 1, 9, 1, 'atendida'),
(60, '2026-09-24', '04:16:00', 2, 1, 3, 1, 'atendida'),
(61, '2026-09-30', '03:09:00', 5, 1, 9, 1, 'cancelada'),
(66, '2026-09-27', '12:00:00', 1, 1, 1, 1, 'atendida'),
(67, '2026-09-25', '08:20:00', 1, 1, 1, 1, 'atendida'),
(68, '2026-09-28', '08:20:00', 1, 1, 1, 1, 'cancelada'),
(69, '2026-09-28', '08:40:00', 1, 1, 1, 1, 'atendida'),
(70, '2026-09-25', '09:00:00', 1, 1, 1, 1, 'atendida'),
(76, '2026-09-25', '16:00:00', 1, 1, 1, 1, 'atendida'),
(77, '2026-09-25', '12:00:00', 1, 1, 1, 1, 'atendida');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `doctor`
--

CREATE TABLE `doctor` (
  `iddoctor` int(11) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `idespecialidad` int(11) DEFAULT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 = Activo, 0 = Inactivo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;

--
-- Volcado de datos para la tabla `doctor`
--

INSERT INTO `doctor` (`iddoctor`, `nombre`, `idespecialidad`, `telefono`, `estado`) VALUES
(1, 'Dr. Alejandro García', 1, '123456789', 1),
(2, 'Dra. Beatriz Martínez', 1, '987654321', 1),
(3, 'Dr. Carlos Rodríguez', 2, '111222333', 1),
(4, 'Dra. Diana López', 2, '444555666', 1),
(5, 'Dr. Enrique Pérez', 3, '777888999', 1),
(6, 'Dra. Flor Ramos', 3, '333222111', 1),
(7, 'Dr. Gabriel Torres', 4, '666555444', 1),
(8, 'Dra. Helena Sánchez', 4, '999888777', 1),
(9, 'Dr. Iván Herrera', 5, '222111000', 1),
(11, 'Dra. Juliana Mendoza', 5, '555444333', 1),
(12, 'Dr. Mario Salazar', 7, '945112233', 1),
(13, 'Dra. Patricia Vega', 7, '945112234', 1),
(14, 'Dr. Renzo Huamán', 7, '945112235', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `especialidad`
--

CREATE TABLE `especialidad` (
  `idespecialidad` int(11) NOT NULL,
  `nombreespecialidad` varchar(255) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;

--
-- Volcado de datos para la tabla `especialidad`
--

INSERT INTO `especialidad` (`idespecialidad`, `nombreespecialidad`, `descripcion`) VALUES
(1, 'Cardiología', 'Especialidad enfocada en el sistema cardiovascular'),
(2, 'Dermatología', 'Especialidad enfocada en la piel y sus enfermedades'),
(3, 'Neurología', 'Especialidad enfocada en el sistema nervioso'),
(4, 'Oftalmología', 'Especialidad enfocada en los ojos y la visión'),
(5, 'Gastroenterología', 'Especialidad enfocada en el sistema digestivo'),
(7, 'Medicina General', 'Atención primaria, chequeos generales y derivación a especialistas');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `paciente`
--

CREATE TABLE `paciente` (
  `idpaciente` int(11) NOT NULL,
  `dni` char(8) NOT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 = Activo, 0 = Inactivo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;

--
-- Volcado de datos para la tabla `paciente`
--

INSERT INTO `paciente` (`idpaciente`, `dni`, `correo`, `password`, `estado`) VALUES
(1, '70000001', 'paciente01@example.com', '$2y$10$7x38fa3H1pmvGnU9hWbNAeIVqbTZpe08M5Dx5PkIkUwa5iQMMFzD6', 1),
(2, '70000002', 'paciente02@example.com', '$2y$10$7x38fa3H1pmvGnU9hWbNAeIVqbTZpe08M5Dx5PkIkUwa5iQMMFzD6', 0),
(3, '70000003', 'paciente03@example.com', '$2y$10$7x38fa3H1pmvGnU9hWbNAeIVqbTZpe08M5Dx5PkIkUwa5iQMMFzD6', 1),
(4, '70000004', 'paciente04@example.com', '$2y$10$7x38fa3H1pmvGnU9hWbNAeIVqbTZpe08M5Dx5PkIkUwa5iQMMFzD6', 1),
(5, '70000005', 'paciente05@example.com', '$2y$10$7x38fa3H1pmvGnU9hWbNAeIVqbTZpe08M5Dx5PkIkUwa5iQMMFzD6', 1),
(7, '70000006', 'paciente06@example.com', '$2y$10$7x38fa3H1pmvGnU9hWbNAeIVqbTZpe08M5Dx5PkIkUwa5iQMMFzD6', 1),
(9, '70000007', 'paciente07@example.com', '$2y$10$7x38fa3H1pmvGnU9hWbNAeIVqbTZpe08M5Dx5PkIkUwa5iQMMFzD6', 1),
(10, '70000008', 'paciente08@example.com', '$2y$10$7x38fa3H1pmvGnU9hWbNAeIVqbTZpe08M5Dx5PkIkUwa5iQMMFzD6', 1),
(11, '70000009', 'paciente09@example.com', '$2y$10$7x38fa3H1pmvGnU9hWbNAeIVqbTZpe08M5Dx5PkIkUwa5iQMMFzD6', 1),
(13, '70000010', 'paciente10@example.com', '$2y$10$7x38fa3H1pmvGnU9hWbNAeIVqbTZpe08M5Dx5PkIkUwa5iQMMFzD6', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `paciente_hospital`
--

CREATE TABLE `paciente_hospital` (
  `dni` char(8) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `fecha_nacimiento` date NOT NULL,
  `sexo` char(1) NOT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `idsede` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;

--
-- Volcado de datos para la tabla `paciente_hospital`
--

INSERT INTO `paciente_hospital` (`dni`, `nombre`, `fecha_nacimiento`, `sexo`, `telefono`, `idsede`) VALUES
('70000001', 'Paciente Prueba Uno', '1999-03-12', 'M', '999999999', 1),
('70000002', 'Paciente Prueba Dos (cuenta inactiva)', '1991-07-22', 'M', '111222333', 1),
('70000003', 'Paciente Prueba Tres', '1998-11-05', 'F', '444555666', 1),
('70000004', 'Paciente Prueba Cuatro', '1981-02-18', 'F', '777888999', 1),
('70000005', 'Paciente Prueba Cinco', '1996-09-30', 'M', '998556234', 1),
('70000006', 'Paciente Prueba Seis', '1996-04-08', 'M', '998556234', 1),
('70000007', 'Paciente Prueba Siete', '2011-06-25', 'M', '987654321', 1),
('70000008', 'Paciente Prueba Ocho', '1993-01-15', 'F', '859675896', 1),
('70000009', 'Paciente Prueba Nueve', '1971-08-03', 'M', '8394455666', 1),
('70000010', 'Paciente Prueba Diez', '2001-10-10', 'M', '987654321', 1),
('70000011', 'María Gutiérrez Morales', '1997-03-24', 'F', '918196001', 1),
('70000012', 'Rodrigo Rodríguez Huamán', '2000-12-21', 'M', '986379402', 3),
('70000013', 'Andrés Flores Cruz', '1982-02-03', 'M', '961559407', 3),
('70000014', 'Rubén Rojas Pérez', '1955-05-27', 'M', '995931034', 1),
('70000015', 'Ximena Sánchez Rojas', '1990-08-21', 'F', '952553419', 3),
('70000016', 'Hugo Morales Rivera', '1966-07-09', 'M', '983503056', 2),
('70000017', 'Rubén Mamani Vásquez', '1998-11-16', 'M', '967242388', 2),
('70000018', 'Elena Espinoza Mendoza', '1979-04-05', 'F', '987101226', 3),
('70000019', 'Raúl Silva Castro', '1958-05-18', 'M', '901845146', 1),
('70000020', 'Hugo Ortiz Paredes', '2003-09-04', 'M', '948932528', 3),
('70000022', 'Verónica Torres Torres', '1941-08-18', 'F', '924896383', 3),
('70000023', 'Iván Vargas Aguilar', '1959-08-04', 'M', '933150983', 3),
('70000026', 'Hugo López Sánchez', '2018-07-24', 'M', '951333872', 2),
('70000027', 'Víctor Morales González', '1969-09-04', 'M', '908013267', 2),
('70000028', 'Raúl López Rivera', '1977-01-13', 'M', '947468723', 2),
('70000029', 'Luis Espinoza Quispe', '2018-12-11', 'M', '900978820', 3),
('70000030', 'Daniel González Silva', '2017-11-28', 'M', '936193990', 3),
('70000031', 'Iván Espinoza Mamani', '1959-06-09', 'M', '935346247', 2),
('70000032', 'Estefanía González García', '1967-10-19', 'F', '911838425', 1),
('70000033', 'Vanessa Chávez Rivera', '1969-09-23', 'F', '949808412', 2),
('70000034', 'Miguel Huamán Flores', '1991-05-20', 'M', '935348740', 1),
('70000035', 'Susana Gutiérrez Martínez', '2025-06-25', 'F', '924278680', 1),
('70000036', 'Julio Flores Quispe', '2021-06-19', 'M', '982620450', 2),
('70000037', 'Ricardo Sánchez Jiménez', '1954-07-20', 'M', '923226025', 2),
('70000038', 'Ximena Morales Gutiérrez', '2005-12-04', 'F', '960733754', 1),
('70000039', 'Iván Díaz Mendoza', '1983-05-28', 'M', '914586850', 1),
('70000040', 'Karina Gómez Espinoza', '1992-01-04', 'F', '996556981', 2),
('70000041', 'Claudia Ortiz Martínez', '1970-01-17', 'F', '983561595', 3),
('70000042', 'Yolanda Ramos Paredes', '1986-11-14', 'F', '956482366', 3),
('70000043', 'Laura Cabrera Mamani', '1987-07-18', 'F', '904436995', 2),
('70000044', 'Iván Cruz Paredes', '1965-12-06', 'M', '914895134', 1),
('70000045', 'Patricia Rodríguez Martínez', '1994-08-20', 'F', '917693676', 1),
('70000046', 'Cristian García Sánchez', '1971-04-06', 'M', '987083172', 2),
('70000047', 'Verónica Silva Vásquez', '1969-10-27', 'F', '986872774', 1),
('70000048', 'Karina Cárdenas Núñez', '1945-04-09', 'F', '971434558', 1),
('70000049', 'Ricardo Rojas Flores', '1998-02-14', 'M', '965876036', 2),
('70000050', 'Cecilia Rodríguez Mamani', '1977-08-01', 'F', '954668893', 2),
('70000051', 'Óscar Núñez Rodríguez', '1976-06-22', 'M', '962729806', 3),
('70000052', 'María Pérez Medina', '2008-08-06', 'F', '904653755', 2),
('70000053', 'Jesús Herrera Ortiz', '2015-08-01', 'M', '980531003', 1),
('70000054', 'Paola Flores Morales', '2009-08-22', 'F', '919374529', 3),
('70000055', 'Fiorella Ramírez Rivera', '1986-02-19', 'F', '904966319', 3),
('70000056', 'Sandra Sánchez Ramos', '1949-02-26', 'F', '990586518', 3),
('70000057', 'Gabriel Herrera Núñez', '2012-07-12', 'M', '972628498', 2),
('70000058', 'Jesús Espinoza Gutiérrez', '1984-04-27', 'M', '914737996', 2),
('70000059', 'Gabriel Vásquez Gómez', '1963-04-12', 'M', '945494808', 1),
('70000060', 'Hugo Herrera Núñez', '1954-04-23', 'M', '977701436', 3),
('70000061', 'Iván Espinoza Vargas', '1965-09-17', 'M', '956855744', 2),
('70000062', 'Hugo Díaz Vásquez', '2010-12-18', 'M', '923374989', 2),
('70000063', 'Susana Díaz Chávez', '1996-06-06', 'F', '940824008', 2),
('70000064', 'Patricia Núñez Sánchez', '2024-10-10', 'F', '977752047', 1),
('70000065', 'Lucía Núñez González', '1952-11-22', 'F', '902294131', 3),
('70000066', 'Paola Silva Cabrera', '1997-09-13', 'F', '977496499', 1),
('70000067', 'Yolanda Sánchez Cruz', '1945-04-09', 'F', '912328120', 2),
('70000068', 'Rodrigo Salazar Chávez', '2021-04-10', 'M', '947134936', 1),
('70000069', 'Milagros Flores Gutiérrez', '2007-02-02', 'F', '924994717', 3),
('70000070', 'Raúl Gutiérrez Paredes', '1956-08-15', 'M', '919065940', 1),
('70000071', 'Iván Mamani Espinoza', '2023-11-27', 'M', '949027874', 1),
('70000072', 'Elena Núñez Pérez', '1965-06-14', 'F', '955125674', 3),
('70000073', 'Susana Huamán Martínez', '1967-02-11', 'F', '945168087', 2),
('70000074', 'Alberto Vargas Cabrera', '1962-11-15', 'M', '903482477', 1),
('70000075', 'Martín Silva Morales', '2005-05-18', 'M', '908613171', 3),
('70000076', 'Teresa Chávez Paredes', '1991-07-27', 'F', '977378263', 3),
('70000077', 'Nataly Torres González', '1990-07-11', 'F', '984044997', 1),
('70000078', 'Manuel Jiménez Castillo', '1955-09-13', 'M', '975339636', 1),
('70000079', 'Manuel Rojas Rojas', '1941-11-05', 'M', '970289517', 1),
('70000080', 'Silvia García Flores', '1973-11-05', 'F', '917459615', 3),
('70000081', 'Lucía Vásquez Núñez', '1956-01-20', 'F', '913431611', 2),
('70000082', 'Diego Rodríguez Martínez', '1984-01-10', 'M', '955623869', 3),
('70000083', 'Laura Gómez Pérez', '1947-07-20', 'F', '937923747', 2),
('70000084', 'Nataly Castro Chávez', '1956-03-03', 'F', '975946474', 1),
('70000085', 'Ximena Salazar Sánchez', '1995-07-19', 'F', '959440640', 3),
('70000086', 'Ángela López Silva', '1962-05-25', 'F', '939533942', 3),
('70000087', 'Martín Martínez Ramos', '1969-01-19', 'M', '952145623', 1),
('70000088', 'Nataly Vargas Cárdenas', '1961-05-27', 'F', '924745171', 1),
('70000089', 'Sandra Mendoza Huamán', '1979-02-26', 'F', '960481754', 3),
('70000090', 'Martín Vargas Sánchez', '1996-08-01', 'M', '998593174', 3),
('70000091', 'José Martínez Martínez', '1987-08-04', 'M', '913826758', 2),
('70000092', 'Yolanda Flores Herrera', '1942-02-27', 'F', '979640537', 2),
('70000093', 'Ximena Vargas Sánchez', '1978-09-21', 'F', '950643171', 3),
('70000094', 'Martín Silva Rodríguez', '2019-06-08', 'M', '929318393', 1),
('70000095', 'José Silva García', '1990-03-05', 'M', '984210205', 1),
('70000096', 'María Gómez Ortiz', '2019-03-24', 'F', '968117758', 3),
('70000097', 'Alberto Reyes Cabrera', '2020-12-26', 'M', '984700766', 3),
('70000098', 'Cristian Aguilar González', '2015-06-20', 'M', '921249985', 2),
('70000099', 'Mónica Chávez Castro', '1961-10-14', 'F', '911836736', 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sede`
--

CREATE TABLE `sede` (
  `idsede` int(11) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 = Activo, 0 = Inactivo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;

--
-- Volcado de datos para la tabla `sede`
--

INSERT INTO `sede` (`idsede`, `nombre`, `direccion`, `telefono`, `estado`) VALUES
(1, 'Sede Lima Centro', 'Av. Abancay 123, Cercado de Lima', '014567890', 1),
(2, 'Sede Miraflores', 'Av. Larco 456, Miraflores', '014567891', 1),
(3, 'Sede San Juan de Lurigancho', 'Av. Próceres de la Independencia 789, SJL', '014567892', 1);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `cita`
--
ALTER TABLE `cita`
  ADD PRIMARY KEY (`idcita`),
  ADD UNIQUE KEY `uq_cita_slot_ocupado` (`slot_ocupado`),
  ADD KEY `idpaciente` (`idpaciente`),
  ADD KEY `iddoctor` (`iddoctor`),
  ADD KEY `especialidad` (`especialidad`),
  ADD KEY `cita_ibfk_4` (`idsede`);

--
-- Indices de la tabla `doctor`
--
ALTER TABLE `doctor`
  ADD PRIMARY KEY (`iddoctor`),
  ADD KEY `idespecialidad` (`idespecialidad`);

--
-- Indices de la tabla `especialidad`
--
ALTER TABLE `especialidad`
  ADD PRIMARY KEY (`idespecialidad`);

--
-- Indices de la tabla `paciente`
--
ALTER TABLE `paciente`
  ADD PRIMARY KEY (`idpaciente`),
  ADD UNIQUE KEY `uq_paciente_dni` (`dni`),
  ADD UNIQUE KEY `uq_paciente_correo` (`correo`);

--
-- Indices de la tabla `paciente_hospital`
--
ALTER TABLE `paciente_hospital`
  ADD PRIMARY KEY (`dni`),
  ADD KEY `paciente_hospital_ibfk_1` (`idsede`);

--
-- Indices de la tabla `sede`
--
ALTER TABLE `sede`
  ADD PRIMARY KEY (`idsede`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `cita`
--
ALTER TABLE `cita`
  MODIFY `idcita` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=78;

--
-- AUTO_INCREMENT de la tabla `doctor`
--
ALTER TABLE `doctor`
  MODIFY `iddoctor` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `especialidad`
--
ALTER TABLE `especialidad`
  MODIFY `idespecialidad` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `paciente`
--
ALTER TABLE `paciente`
  MODIFY `idpaciente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT de la tabla `sede`
--
ALTER TABLE `sede`
  MODIFY `idsede` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `cita`
--
ALTER TABLE `cita`
  ADD CONSTRAINT `cita_ibfk_1` FOREIGN KEY (`idpaciente`) REFERENCES `paciente` (`idpaciente`),
  ADD CONSTRAINT `cita_ibfk_2` FOREIGN KEY (`iddoctor`) REFERENCES `doctor` (`iddoctor`),
  ADD CONSTRAINT `cita_ibfk_3` FOREIGN KEY (`especialidad`) REFERENCES `especialidad` (`idespecialidad`),
  ADD CONSTRAINT `cita_ibfk_4` FOREIGN KEY (`idsede`) REFERENCES `sede` (`idsede`);

--
-- Filtros para la tabla `doctor`
--
ALTER TABLE `doctor`
  ADD CONSTRAINT `doctor_ibfk_1` FOREIGN KEY (`idespecialidad`) REFERENCES `especialidad` (`idespecialidad`);

--
-- Filtros para la tabla `paciente`
--
ALTER TABLE `paciente`
  ADD CONSTRAINT `paciente_ibfk_2` FOREIGN KEY (`dni`) REFERENCES `paciente_hospital` (`dni`);

--
-- Filtros para la tabla `paciente_hospital`
--
ALTER TABLE `paciente_hospital`
  ADD CONSTRAINT `paciente_hospital_ibfk_1` FOREIGN KEY (`idsede`) REFERENCES `sede` (`idsede`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
