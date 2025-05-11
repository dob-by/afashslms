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

INSERT INTO users (user_id, username, email, password, provider, role, created_at)
VALUES ('testuser1', '김도비', 'test@naver.com', 'abc123', 'local', 'STUDENT', CURRENT_TIMESTAMP);

INSERT INTO laptop (device_id, model_name, ip, status, current_state, manage_number, user_id)
VALUES ('TEST-123', 'LG Gram', '192.168.0.10', 'AVAILABLE', '학생보유', 999, 'testuser1');