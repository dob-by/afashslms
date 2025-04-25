INSERT INTO users (
    user_id,
    password,
    username,
    email,
    provider,
    role,
    created_at
) VALUES (
             'admin',
             '$2a$10$kXuU1gDZQJVD7WTV3/3B7eh8Hc7trLzMDO0XZZk5Z6zVK55L0Ucnu', -- "1234"를 BCrypt로 암호화한 것
             '관리자',
             'admin@test.com',
             'local',
             'TOP_ADMIN',
             CURRENT_TIMESTAMP
         );