-- Modération de contenu (Nyé-A5, §13.6) et rejet d'une alerte par un administrateur (§13.6).
ALTER TABLE alerts ADD COLUMN moderation_flagged BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE alerts ADD COLUMN moderation_reason VARCHAR(1000);
ALTER TABLE alerts ADD COLUMN rejection_reason VARCHAR(1000);
