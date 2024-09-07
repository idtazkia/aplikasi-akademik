CREATE TABLE `event` (
                         `id` varchar(45) NOT NULL,
                         `id_tahun_akademik` varchar(45) DEFAULT NULL,
                         `event_name` varchar(255) DEFAULT NULL,
                         `event_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                         `file` varchar(45) DEFAULT NULL,
                         `event_type` varchar(25) DEFAULT NULL,
                         `event_link` longtext DEFAULT NULL,
                         `status` varchar(15) DEFAULT NULL,
                         `user_insert` varchar(45) DEFAULT NULL,
                         `user_update` varchar(45) DEFAULT NULL,
                         `user_delete` varchar(45) DEFAULT NULL,
                         `date_insert` datetime DEFAULT NULL,
                         `date_update` datetime DEFAULT NULL,
                         `date_delete` datetime DEFAULT NULL,
                         `schedule_start` datetime DEFAULT NULL,
                         `schedule_end` datetime DEFAULT NULL,
                         `schedule_publish` datetime DEFAULT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE `event_absensi` (
                                 `id` varchar(45) NOT NULL,
                                 `id_event` varchar(45) DEFAULT NULL,
                                 `id_tahun_akademik` varchar(45) DEFAULT NULL,
                                 `id_mahasiswa` varchar(45) DEFAULT NULL,
                                 `deskripsi` longtext DEFAULT NULL,
                                 `waktu_absen` datetime DEFAULT NULL,
                                 `file` longtext DEFAULT NULL,
                                 `status` varchar(25) DEFAULT NULL,
                                 `date_insert` datetime DEFAULT NULL,
                                 `date_update` datetime DEFAULT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

