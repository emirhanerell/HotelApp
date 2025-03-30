-- end_date sütununu kaldır
ALTER TABLE reservations DROP COLUMN IF EXISTS end_date;

-- check_out_date sütununu end_date olarak yeniden adlandır (eğer gerekirse)
-- ALTER TABLE reservations RENAME COLUMN check_out_date TO end_date; 