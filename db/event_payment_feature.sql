-- Paid events (Annonces) — same payment model as projects
-- Run on database `socialnetwork` (MariaDB / MySQL).
-- Requires: user.wallet_balance (see payment_schema.sql)

ALTER TABLE `event`
  ADD COLUMN `is_paid` tinyint(1) NOT NULL DEFAULT 0,
  ADD COLUMN `price` decimal(10,2) DEFAULT NULL;

UPDATE `event`
SET `price` = 0.00
WHERE `price` IS NULL;

CREATE TABLE IF NOT EXISTS `event_payment` (
  `id` uuid NOT NULL DEFAULT (uuid()),
  `event_id` uuid NOT NULL,
  `payer_id` uuid NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `status` enum('SUCCESS','FAILED') NOT NULL,
  `card_last4` varchar(4) DEFAULT NULL,
  `payment_reference` varchar(64) NOT NULL,
  `created_at` datetime(6) DEFAULT current_timestamp(6),
  `paid_at` datetime(6) DEFAULT NULL,
  `refunded` tinyint(1) NOT NULL DEFAULT 0,
  `refunded_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_payment_reference` (`payment_reference`),
  KEY `idx_event_payment_event` (`event_id`),
  KEY `idx_event_payment_payer` (`payer_id`),
  CONSTRAINT `fk_event_payment_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
