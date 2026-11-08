
-- ==========================================
-- 1. JOURNEY TABLES (existing functionality)
-- ==========================================

CREATE TABLE IF NOT EXISTS journey (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       journey_date DATE DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS journey_details (
                                               id INT AUTO_INCREMENT PRIMARY KEY,
                                               journey_id INT NOT NULL,
                                               distance DOUBLE,
                                               travel_time DOUBLE,
                                               speed DOUBLE,
                                               FOREIGN KEY (journey_id) REFERENCES journey(id)
    );

-- ==========================================
-- 2. UNIT TABLE
-- Language-independent unit information
-- ==========================================

CREATE TABLE IF NOT EXISTS unit (
                                    unit_id INT PRIMARY KEY,
                                    unit_key VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(30) NOT NULL,
    symbol VARCHAR(20)
    );

-- ==========================================
-- 3. UNIT TRANSLATION TABLE
-- Translations in five languages
-- ==========================================

CREATE TABLE IF NOT EXISTS unit_translation (
                                                unit_id INT NOT NULL,
                                                language_code VARCHAR(5) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (unit_id, language_code),
    FOREIGN KEY (unit_id) REFERENCES unit(unit_id)
    );

-- ==========================================
-- 4. CONVERSION HISTORY TABLE
-- ==========================================

CREATE TABLE IF NOT EXISTS conversion_history (
                                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                                  input_value DOUBLE NOT NULL,
                                                  from_unit_id INT NOT NULL,
                                                  to_unit_id INT NOT NULL,
                                                  result_value DOUBLE NOT NULL,
                                                  language_code VARCHAR(5) NOT NULL,
    converted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_unit_id) REFERENCES unit(unit_id),
    FOREIGN KEY (to_unit_id) REFERENCES unit(unit_id)
    );

-- ==========================================
-- 5. INSERT UNIT DATA
-- ==========================================

MERGE INTO unit KEY(unit_id) VALUES
    (1, 'celsius', 'temperature', '°C'),
    (2, 'fahrenheit', 'temperature', '°F'),
    (3, 'kelvin', 'temperature', 'K'),
    (4, 'kmh', 'speed', 'km/h'),
    (5, 'mph', 'speed', 'mph'),
    (6, 'kilometre', 'distance', 'km'),
    (7, 'mile', 'distance', 'mi'),
    (8, 'second', 'time', 's'),
    (9, 'hour', 'time', 'h');

-- ==========================================
-- 6. ENGLISH TRANSLATIONS
-- ==========================================

MERGE INTO unit_translation KEY(unit_id, language_code) VALUES
    (1, 'en', 'Celsius', 'Temperature measured in degrees Celsius'),
    (2, 'en', 'Fahrenheit', 'Temperature measured in degrees Fahrenheit'),
    (3, 'en', 'Kelvin', 'Temperature measured in kelvin'),
    (4, 'en', 'Kilometres per hour', 'Speed measured in kilometres per hour'),
    (5, 'en', 'Miles per hour', 'Speed measured in miles per hour'),
    (6, 'en', 'Kilometre', 'Distance measured in kilometres'),
    (7, 'en', 'Mile', 'Distance measured in miles'),
    (8, 'en', 'Second', 'Time measured in seconds'),
    (9, 'en', 'Hour', 'Time measured in hours');

-- ==========================================
-- 7. FINNISH TRANSLATIONS
-- ==========================================

MERGE INTO unit_translation KEY(unit_id, language_code) VALUES
    (1, 'fi', 'Celsius', 'Lämpötila Celsius-asteina'),
    (2, 'fi', 'Fahrenheit', 'Lämpötila Fahrenheit-asteina'),
    (3, 'fi', 'Kelvin', 'Lämpötila kelvineinä'),
    (4, 'fi', 'Kilometriä tunnissa', 'Nopeus kilometreinä tunnissa'),
    (5, 'fi', 'Mailia tunnissa', 'Nopeus maileina tunnissa'),
    (6, 'fi', 'Kilometri', 'Etäisyys kilometreinä'),
    (7, 'fi', 'Maili', 'Etäisyys maileina'),
    (8, 'fi', 'Sekunti', 'Aika sekunteina'),
    (9, 'fi', 'Tunti', 'Aika tunteina');

-- ==========================================
-- 8. SWEDISH TRANSLATIONS
-- ==========================================

MERGE INTO unit_translation KEY(unit_id, language_code) VALUES
    (1, 'sv', 'Celsius', 'Temperatur mätt i grader Celsius'),
    (2, 'sv', 'Fahrenheit', 'Temperatur mätt i grader Fahrenheit'),
    (3, 'sv', 'Kelvin', 'Temperatur mätt i kelvin'),
    (4, 'sv', 'Kilometer per timme', 'Hastighet mätt i kilometer per timme'),
    (5, 'sv', 'Miles per timme', 'Hastighet mätt i miles per timme'),
    (6, 'sv', 'Kilometer', 'Avstånd mätt i kilometer'),
    (7, 'sv', 'Engelsk mil', 'Avstånd mätt i miles'),
    (8, 'sv', 'Sekund', 'Tid mätt i sekunder'),
    (9, 'sv', 'Timme', 'Tid mätt i timmar');

-- ==========================================
-- 9. JAPANESE TRANSLATIONS
-- ==========================================

MERGE INTO unit_translation KEY(unit_id, language_code) VALUES
    (1, 'ja', '摂氏', '摂氏で測定された温度'),
    (2, 'ja', '華氏', '華氏で測定された温度'),
    (3, 'ja', 'ケルビン', 'ケルビンで測定された温度'),
    (4, 'ja', '時速キロメートル', '時速キロメートルで測定された速度'),
    (5, 'ja', '時速マイル', '時速マイルで測定された速度'),
    (6, 'ja', 'キロメートル', 'キロメートルで測定された距離'),
    (7, 'ja', 'マイル', 'マイルで測定された距離'),
    (8, 'ja', '秒', '秒で測定された時間'),
    (9, 'ja', '時間', '時間単位で測定された時間');

-- ==========================================
-- 10. ARABIC TRANSLATIONS
-- ==========================================

MERGE INTO unit_translation KEY(unit_id, language_code) VALUES
    (1, 'ar', 'مئوية', 'درجة الحرارة مقاسة بالدرجة المئوية'),
    (2, 'ar', 'فهرنهايت', 'درجة الحرارة مقاسة بالفهرنهايت'),
    (3, 'ar', 'كلفن', 'درجة الحرارة مقاسة بالكلفن'),
    (4, 'ar', 'كيلومتر في الساعة', 'السرعة مقاسة بالكيلومتر في الساعة'),
    (5, 'ar', 'ميل في الساعة', 'السرعة مقاسة بالميل في الساعة'),
    (6, 'ar', 'كيلومتر', 'المسافة مقاسة بالكيلومترات'),
    (7, 'ar', 'ميل', 'المسافة مقاسة بالأميال'),
    (8, 'ar', 'ثانية', 'الوقت مقاس بالثواني'),
    (9, 'ar', 'ساعة', 'الوقت مقاس بالساعات');
