ALTER TABLE `project`
  ADD COLUMN `is_paid` tinyint(1) NOT NULL DEFAULT 0,
  ADD COLUMN `price` decimal(10,2) DEFAULT NULL;

UPDATE `project`
SET `price` = 0.00
WHERE `price` IS NULL;

CREATE TABLE `project_payment` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `project_id` char(36) NOT NULL,
  `payer_id` char(36) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `status` enum('SUCCESS','FAILED') NOT NULL,
  `card_last4` varchar(4) DEFAULT NULL,
  `payment_reference` varchar(64) NOT NULL,
  `created_at` datetime(6) DEFAULT current_timestamp(6),
  `paid_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_payment_reference` (`payment_reference`),
  KEY `idx_project_payment_project` (`project_id`),
  KEY `idx_project_payment_payer` (`payer_id`),
  CONSTRAINT `fk_project_payment_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_project_payment_payer` FOREIGN KEY (`payer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- Portefeuille virtuel (EUR) + suivi des remboursements sur project_payment
-- Exécuter sur la base `socialnetwork` (MariaDB / MySQL).

ALTER TABLE `user`
  ADD COLUMN `wallet_balance` decimal(10,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `project_payment`
  ADD COLUMN `refunded` tinyint(1) NOT NULL DEFAULT 0,
  ADD COLUMN `refunded_at` datetime(6) DEFAULT NULL;

-- Moyens de paiement enregistrés (démo : last4 + titulaire + expiration, pas le PAN complet)
-- Exécuter sur la base socialnetwork.

CREATE TABLE IF NOT EXISTS user_payment_method (
  id CHAR(36) NOT NULL,
  user_id CHAR(36) NOT NULL,
  cardholder_name VARCHAR(120) NOT NULL,
  card_last4 VARCHAR(4) NOT NULL,
  expiry_month VARCHAR(2) NOT NULL,
  expiry_year VARCHAR(4) NOT NULL,
  label VARCHAR(80) DEFAULT NULL,
  created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_upm_user (user_id),
  CONSTRAINT fk_upm_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

