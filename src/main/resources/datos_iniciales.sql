UPDATE categoria SET activo = true WHERE lower(nombre) = 'hamburguesas';

INSERT INTO categoria (nombre, activo) VALUES 
('Hamburguesas', true),
('Bebidas', true),
('Papas y Entradas', true),
('Combos', true)
ON CONFLICT (lower(nombre)) DO UPDATE SET activo = true;

INSERT INTO producto (nombre, descripcion, precio, stock, activo, categoria_id) VALUES 
('Hamburguesa Clásica', 'Carne 150g, lechuga, tomate y queso', 16000.00, 40, true, (SELECT id FROM categoria WHERE lower(nombre) = 'hamburguesas' LIMIT 1)),
('Hamburguesa Especial', 'Doble carne, tocineta, queso cheddar y salsa especial', 24000.00, 30, true, (SELECT id FROM categoria WHERE lower(nombre) = 'hamburguesas' LIMIT 1)),
('Gaseosa 400ml', 'Bebida gaseosa en botella', 4500.00, 80, true, (SELECT id FROM categoria WHERE lower(nombre) = 'bebidas' LIMIT 1)),
('Jugo Natural', 'Jugo en agua o leche sabores variados', 6000.00, 25, true, (SELECT id FROM categoria WHERE lower(nombre) = 'bebidas' LIMIT 1)),
('Papas a la Francesa', 'Porción de 200g crocantes con sal marina', 7000.00, 50, true, (SELECT id FROM categoria WHERE lower(nombre) = 'papas y entradas' LIMIT 1));

INSERT INTO cliente (tipo_documento, numero_documento, nombre, apellido, telefono, correo, activo) VALUES 
('Cédula de ciudadanía', '1098765432', 'Carlos', 'Gómez', '3101234567', 'carlos.gomez@correo.com', true),
('NIT', '901234567-8', 'Inversiones', 'Alimentos SAS', '3209876543', 'contacto@alimentos.com', true)
ON CONFLICT (numero_documento) DO NOTHING;
