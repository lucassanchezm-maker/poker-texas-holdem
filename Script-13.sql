-- 1. Crear la base de datos del juego si es que no existe
CREATE DATABASE IF NOT EXISTS poker_db;

-- 2. Decirle a MariaDB que use esta base de datos para lo que viene abajo
USE poker_db;

-- 3. Tabla para el historial de manos (Mapeado exacto de tu clase RoundRecord.java)
CREATE TABLE IF NOT EXISTS poker_rounds (
    id INT AUTO_INCREMENT PRIMARY KEY,
    round_number INT NOT NULL,
    player_cards VARCHAR(50) NOT NULL,
    community_cards VARCHAR(100) NOT NULL,
    player_hand_name VARCHAR(50) NOT NULL,
    cpu_hand_name VARCHAR(50) NOT NULL,
    resultado VARCHAR(20) NOT NULL,
    delta INT NOT NULL,
    fichas INT NOT NULL,
    timestamp VARCHAR(30) NOT NULL
);

-- 4. Tabla para guardar las estadísticas generales (Mapeado de tu GameState.java)
CREATE TABLE IF NOT EXISTS poker_state (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_chips INT DEFAULT 1000,
    cpu_chips INT DEFAULT 1000,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    ties INT DEFAULT 0,
    streak INT DEFAULT 0,
    best_streak INT DEFAULT 0,
    profit INT DEFAULT 0
);

-- Aseguramos que estamos trabajando sobre la base de datos correcta
USE poker_db;

-- 1. Insertamos el estado inicial en poker_state (Datos de tus estadísticas de GameState.java)
INSERT INTO poker_state (player_chips, cpu_chips, wins, losses, ties, streak, best_streak, profit)
VALUES (1250, 750, 5, 3, 1, 2, 4, 250);

-- 2. Insertamos tres rondas de prueba en poker_rounds (Mapeando tu estructura de RoundRecord.java)
INSERT INTO poker_rounds (round_number, player_cards, community_cards, player_hand_name, cpu_hand_name, resultado, delta, fichas, timestamp)
VALUES 
(1, 'A♠ K♦', '10♥ Q♣ 2♠ J♦ 5♣', 'Escalera', 'Par de Reinas', 'Gano', 200, 1200, '23/06/2026 00:05:12'),
(2, '7♣ 8♣', '2♥ 9♦ Q♠ K♣ 3♦', 'Carta Alta', 'Doble Par', 'Perdio', -50, 1150, '23/06/2026 00:08:44'),
(3, 'Q♥ Q♦', '4♠ Q♣ J♣ 2♥ 9♠', 'Trio', 'Par de Jacks', 'Gano', 100, 1250, '23/06/2026 00:12:01');

USE db_tecsup_jg; -- Cambia por el nombre de tu base de datos

CREATE TABLE IF NOT EXISTS historial_partidas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    round_number INT NOT NULL,
    player_cards VARCHAR(50),
    community_cards VARCHAR(50),
    player_hand_name VARCHAR(50)
    -- ... tus demás campos
);

