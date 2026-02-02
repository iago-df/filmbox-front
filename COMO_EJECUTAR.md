# Cómo ejecutar la app y que los cambios se vean

## Si el emulador no muestra el XML nuevo (textos, pantalla de login)

El emulador suele seguir usando una versión antigua de la app hasta que haces una **instalación limpia**. Sigue estos pasos **en este orden**:

1. **Desinstala la app del emulador**
   - Mantén pulsado el icono de FilmBox en el emulador → Desinstalar  
   - O: Ajustes → Apps → FilmBox → Desinstalar

2. **En Android Studio / Cursor**
   - **Build → Clean Project** (espera a que termine)
   - **Build → Rebuild Project** (espera a que termine)

3. **Vuelve a instalar y ejecutar**
   - **Run → Run 'app'** (o el botón de ejecutar)

4. **Comprueba que es la versión nueva**
   - Al abrir la app debe salir un Toast: **"App v1.2 - Pantalla de login cargada"**
   - Si ves ese Toast, el XML y el código nuevos están cargados.

---

## Si las peticiones no llegan al servidor

1. **Servidor en tu PC**
   - Debe estar levantado **antes** de pulsar "Iniciar sesión" en la app.
   - Debe escuchar en el puerto **8000** (o el que uses).
   - En Django: `python manage.py runserver 0.0.0.0:8000`  
     (0.0.0.0 permite conexiones desde el emulador; solo 127.0.0.1 a veces falla).

2. **Emulador**
   - La app usa la URL: `http://10.0.2.2:8000/`  
   - 10.0.2.2 es la IP que el emulador usa para “tu PC”.

3. **Comprobar en la app**
   - Escribe usuario y contraseña y pulsa **Iniciar sesión**.
   - Debe salir el Toast: **"Enviando petición al servidor..."**  
     → Si sale, el botón y el código de login se ejecutan.
   - En **Logcat** (filtro por "RetrofitClient" o "LoginActivity") verás la petición HTTP y la URL exacta.

4. **Si sigue sin llegar**
   - Revisa en Logcat el mensaje de error (ej. "Connection refused", "Failed to connect").
   - Comprueba que el servidor está en 0.0.0.0:8000 y que el firewall no bloquea el puerto 8000.

---

## Dispositivo físico (móvil real)

En el móvil, 10.0.2.2 no vale. Cambia la URL en `RetrofitClient.java`:

- `BASE_URL = "http://IP_DE_TU_PC:8000/"`  
  (ej: `"http://192.168.1.100:8000/"` si tu PC tiene esa IP en la WiFi).
- El móvil y la PC deben estar en la misma red WiFi.
