
CREATE TABLE IF NOT EXISTS hall_details (
    hall_id                    INT UNSIGNED NOT NULL,
    hall_code                  VARCHAR(20)  NOT NULL,
    established_year           INT          NOT NULL DEFAULT 1980,
    address                    VARCHAR(255) NOT NULL DEFAULT 'RUET Campus, Kazla, Rajshahi-6204',
    description                TEXT         DEFAULT NULL,
    total_floors               INT          NOT NULL DEFAULT 4,
    total_staff                INT          NOT NULL DEFAULT 25,
    non_residential_students   INT          NOT NULL DEFAULT 150,
    assistant_provosts         TEXT         DEFAULT NULL,
    office_contact             VARCHAR(30)  DEFAULT '02588860000',
    emergency_contact          VARCHAR(30)  DEFAULT '01711000000',
    email                      VARCHAR(100) DEFAULT 'hall.office@ruet.ac.bd',
    PRIMARY KEY (hall_id),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_buildings (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    building_name              VARCHAR(100) NOT NULL DEFAULT 'Main Residential Complex',
    floors_count               INT          NOT NULL DEFAULT 4,
    rooms_per_floor            INT          NOT NULL DEFAULT 20,
    total_rooms                INT          NOT NULL DEFAULT 80,
    residential_rooms          INT          NOT NULL DEFAULT 74,
    guest_rooms                INT          NOT NULL DEFAULT 2,
    common_rooms               INT          NOT NULL DEFAULT 1,
    study_rooms                INT          NOT NULL DEFAULT 1,
    prayer_room                INT          NOT NULL DEFAULT 1,
    dining_hall                INT          NOT NULL DEFAULT 1,
    kitchen                    INT          NOT NULL DEFAULT 1,
    hall_office                INT          NOT NULL DEFAULT 1,
    provost_office             INT          NOT NULL DEFAULT 1,
    store_room                 INT          NOT NULL DEFAULT 2,
    medical_room               INT          NOT NULL DEFAULT 1,
    tv_room                    INT          NOT NULL DEFAULT 1,
    reading_room               INT          NOT NULL DEFAULT 1,
    it_room                    INT          NOT NULL DEFAULT 1,
    laundry_area               INT          NOT NULL DEFAULT 2,
    parking_area               VARCHAR(100) DEFAULT 'Bicycle & Motorcycle Garage (Capacity: 120)',
    other_facilities           TEXT         DEFAULT 'Table Tennis Room, Gymnasium, Generator Substation',
    PRIMARY KEY (id),
    UNIQUE KEY uq_hall_building (hall_id),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_floors (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    floor_number               INT          NOT NULL,
    floor_name                 VARCHAR(50)  NOT NULL,
    total_rooms                INT          NOT NULL DEFAULT 20,
    washrooms_count            INT          NOT NULL DEFAULT 4,
    toilets_count              INT          NOT NULL DEFAULT 12,
    water_taps                 INT          NOT NULL DEFAULT 16,
    emergency_exits            INT          NOT NULL DEFAULT 2,
    windows_count              INT          NOT NULL DEFAULT 40,
    doors_count                INT          NOT NULL DEFAULT 22,
    PRIMARY KEY (id),
    UNIQUE KEY uq_hall_floor (hall_id, floor_number),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_details (
    room_id                    INT UNSIGNED NOT NULL,
    room_type                  VARCHAR(50)  NOT NULL DEFAULT 'STANDARD',
    floor_number               INT          NOT NULL DEFAULT 1,
    room_size_sqft             DECIMAL(6,2) NOT NULL DEFAULT 240.00,
    window_count               INT          NOT NULL DEFAULT 2,
    door_count                 INT          NOT NULL DEFAULT 1,
    has_balcony                TINYINT(1)   NOT NULL DEFAULT 1,
    has_attached_bath          TINYINT(1)   NOT NULL DEFAULT 0,
    condition_status           VARCHAR(30)  NOT NULL DEFAULT 'GOOD',
    PRIMARY KEY (room_id),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_electrical (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    room_id                    INT UNSIGNED NOT NULL,
    ac_count                   INT          NOT NULL DEFAULT 0,
    ceiling_fan_count          INT          NOT NULL DEFAULT 2,
    wall_fan_count             INT          NOT NULL DEFAULT 0,
    exhaust_fan_count          INT          NOT NULL DEFAULT 0,
    tube_light_count           INT          NOT NULL DEFAULT 2,
    led_light_count            INT          NOT NULL DEFAULT 2,
    bulb_count                 INT          NOT NULL DEFAULT 0,
    emergency_light_count      INT          NOT NULL DEFAULT 1,
    night_light_count          INT          NOT NULL DEFAULT 1,
    switch_count               INT          NOT NULL DEFAULT 6,
    socket_count               INT          NOT NULL DEFAULT 4,
    power_outlet_count         INT          NOT NULL DEFAULT 2,
    other_equipment            VARCHAR(255) DEFAULT 'Wall Clock, Calling Bell',
    PRIMARY KEY (id),
    UNIQUE KEY uq_room_elec (room_id),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_furniture (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    room_id                    INT UNSIGNED NOT NULL,
    bed_count                  INT          NOT NULL DEFAULT 4,
    table_count                INT          NOT NULL DEFAULT 4,
    chair_count                INT          NOT NULL DEFAULT 4,
    wardrobe_count             INT          NOT NULL DEFAULT 4,
    bookshelf_count            INT          NOT NULL DEFAULT 2,
    mattress_count             INT          NOT NULL DEFAULT 4,
    mirror_count               INT          NOT NULL DEFAULT 1,
    curtain_count              INT          NOT NULL DEFAULT 2,
    dustbin_count              INT          NOT NULL DEFAULT 1,
    good_condition_count       INT          NOT NULL DEFAULT 21,
    damaged_count              INT          NOT NULL DEFAULT 0,
    under_repair_count         INT          NOT NULL DEFAULT 0,
    missing_count              INT          NOT NULL DEFAULT 0,
    furniture_remarks          VARCHAR(255) DEFAULT 'All furniture numbered and verified.',
    PRIMARY KEY (id),
    UNIQUE KEY uq_room_furn (room_id),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_equipment_inventory (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    item_code                  VARCHAR(50)  NOT NULL,
    category                   VARCHAR(30)  NOT NULL,
    equipment_type             VARCHAR(50)  NOT NULL,
    location_type              VARCHAR(30)  NOT NULL DEFAULT 'ROOM',
    room_number                INT          DEFAULT NULL,
    floor_number               INT          DEFAULT NULL,
    specific_location          VARCHAR(100) DEFAULT NULL,
    brand_model                VARCHAR(100) DEFAULT NULL,
    capacity_rating            VARCHAR(50)  DEFAULT NULL,
    install_date               DATE         DEFAULT '2023-01-15',
    condition_status           VARCHAR(30)  NOT NULL DEFAULT 'WORKING',
    last_service_date          DATE         DEFAULT '2026-05-10',
    next_service_date          DATE         DEFAULT '2026-11-10',
    remarks                    VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_item_code (hall_id, item_code),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_washrooms (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    floor_number               INT          NOT NULL,
    location_name              VARCHAR(100) NOT NULL,
    toilets_count              INT          NOT NULL DEFAULT 6,
    urinals_count              INT          NOT NULL DEFAULT 4,
    showers_count              INT          NOT NULL DEFAULT 4,
    water_taps                 INT          NOT NULL DEFAULT 8,
    basins_count               INT          NOT NULL DEFAULT 4,
    mirrors_count              INT          NOT NULL DEFAULT 4,
    exhaust_fans               INT          NOT NULL DEFAULT 2,
    lights_count               INT          NOT NULL DEFAULT 4,
    water_heaters              INT          NOT NULL DEFAULT 1,
    water_filters              INT          NOT NULL DEFAULT 1,
    condition_status           VARCHAR(30)  NOT NULL DEFAULT 'GOOD',
    last_maintenance_date      DATE         DEFAULT '2026-08-15',
    next_maintenance_date      DATE         DEFAULT '2026-10-15',
    PRIMARY KEY (id),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_water_system (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    source_type                VARCHAR(100) DEFAULT 'Deep Tube Well + Municipal Supply',
    deep_tube_well             VARCHAR(100) DEFAULT '1 x 10HP Submersible Deep Well',
    pump_count                 INT          NOT NULL DEFAULT 2,
    tank_count                 INT          NOT NULL DEFAULT 4,
    total_tank_capacity_liters INT          NOT NULL DEFAULT 20000,
    filters_count              INT          NOT NULL DEFAULT 8,
    purifiers_count            INT          NOT NULL DEFAULT 4,
    supply_schedule            VARCHAR(255) DEFAULT '24/7 Continuous Automated Supply (Sensors)',
    drinking_water_points      INT          NOT NULL DEFAULT 6,
    tap_count                  INT          NOT NULL DEFAULT 64,
    pump_condition             VARCHAR(30)  NOT NULL DEFAULT 'WORKING',
    tank_condition             VARCHAR(30)  NOT NULL DEFAULT 'EXCELLENT',
    last_cleaning_date         DATE         DEFAULT '2026-07-01',
    next_cleaning_date         DATE         DEFAULT '2026-10-01',
    PRIMARY KEY (id),
    UNIQUE KEY uq_hall_water (hall_id),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_common_areas (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    area_name                  VARCHAR(100) NOT NULL,
    capacity                   INT          NOT NULL DEFAULT 50,
    ac_count                   INT          NOT NULL DEFAULT 0,
    fan_count                  INT          NOT NULL DEFAULT 4,
    light_count                INT          NOT NULL DEFAULT 6,
    table_count                INT          NOT NULL DEFAULT 6,
    chair_count                INT          NOT NULL DEFAULT 24,
    condition_status           VARCHAR(30)  NOT NULL DEFAULT 'GOOD',
    responsible_staff          VARCHAR(100) DEFAULT 'Md. Rafiqul Islam (Senior Caretaker)',
    remarks                    VARCHAR(255) DEFAULT 'Open 6:00 AM - 11:00 PM daily',
    PRIMARY KEY (id),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_kitchen_dining (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    kitchen_size_sqft          DECIMAL(6,2) DEFAULT 650.00,
    cooking_area_desc          VARCHAR(255) DEFAULT 'Central Industrial Cooking Block with S/S Countertops',
    stove_count                INT          NOT NULL DEFAULT 4,
    gas_burner_count           INT          NOT NULL DEFAULT 8,
    refrigerator_count         INT          NOT NULL DEFAULT 2,
    freezer_count              INT          NOT NULL DEFAULT 2,
    water_filter_count         INT          NOT NULL DEFAULT 2,
    sink_count                 INT          NOT NULL DEFAULT 4,
    exhaust_fan_count          INT          NOT NULL DEFAULT 4,
    kitchen_staff_count        INT          NOT NULL DEFAULT 8,
    dining_capacity            INT          NOT NULL DEFAULT 200,
    dining_table_count         INT          NOT NULL DEFAULT 35,
    dining_chair_count         INT          NOT NULL DEFAULT 210,
    dining_fans                INT          NOT NULL DEFAULT 16,
    dining_lights              INT          NOT NULL DEFAULT 24,
    dining_ac                  INT          NOT NULL DEFAULT 2,
    wash_basin_count           INT          NOT NULL DEFAULT 10,
    PRIMARY KEY (id),
    UNIQUE KEY uq_hall_kd (hall_id),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_maintenance_records (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    record_code                VARCHAR(50)  NOT NULL,
    location                   VARCHAR(100) NOT NULL,
    room_number                INT          DEFAULT NULL,
    equipment_category         VARCHAR(50)  NOT NULL,
    problem_description        TEXT         NOT NULL,
    reported_by                VARCHAR(100) NOT NULL,
    report_date                DATE         NOT NULL,
    assigned_person            VARCHAR(100) DEFAULT NULL,
    priority                   VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    status                     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    estimated_cost             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    actual_cost                DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    completion_date            DATE         DEFAULT NULL,
    remarks                    TEXT         DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_mnt_code (record_code),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_cleaning_schedules (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    area_type                  VARCHAR(50)  NOT NULL,
    frequency                  VARCHAR(30)  NOT NULL DEFAULT 'DAILY',
    responsible_staff          VARCHAR(100) NOT NULL,
    status                     VARCHAR(30)  NOT NULL DEFAULT 'COMPLETED',
    last_cleaned_date          DATE         DEFAULT '2026-09-02',
    next_scheduled_date        DATE         DEFAULT '2026-09-03',
    remarks                    VARCHAR(255) DEFAULT 'Sanitized and inspected',
    PRIMARY KEY (id),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_contacts (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    category                   VARCHAR(30)  NOT NULL,
    designation                VARCHAR(100) NOT NULL,
    name                       VARCHAR(100) NOT NULL,
    phone                      VARCHAR(30)  NOT NULL,
    email                      VARCHAR(100) DEFAULT NULL,
    availability               VARCHAR(50)  NOT NULL DEFAULT 'Office Hours',
    PRIMARY KEY (id),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_utility_finances (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    billing_month              VARCHAR(50)  NOT NULL,
    electricity_bill           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    water_bill                 DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    internet_bill              DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    gas_bill                   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    maintenance_cost           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cleaning_cost              DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_expense              DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_status             VARCHAR(30)  NOT NULL DEFAULT 'PAID',
    payment_date               DATE         DEFAULT NULL,
    remarks                    VARCHAR(255) DEFAULT 'Cleared by University Finance Division',
    PRIMARY KEY (id),
    UNIQUE KEY uq_hall_month (hall_id, billing_month),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_assets (
    id                         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id                    INT UNSIGNED NOT NULL,
    asset_code                 VARCHAR(50)  NOT NULL,
    asset_name                 VARCHAR(100) NOT NULL,
    category                   VARCHAR(50)  NOT NULL,
    quantity                   INT          NOT NULL DEFAULT 1,
    location                   VARCHAR(100) DEFAULT NULL,
    room_number                INT          DEFAULT NULL,
    floor_number               INT          DEFAULT NULL,
    purchase_date              DATE         DEFAULT '2023-03-20',
    purchase_price             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    condition_status           VARCHAR(30)  NOT NULL DEFAULT 'GOOD',
    warranty_info              VARCHAR(100) DEFAULT '3 Years Comprehensive Warranty',
    assigned_person            VARCHAR(100) DEFAULT 'Estate Officer',
    remarks                    VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_asset_code (hall_id, asset_code),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
