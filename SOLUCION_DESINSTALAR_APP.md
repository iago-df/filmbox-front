# ⚠️ SOLUCIÓN: Desinstalar completamente la app antigua

## El problema
El emulador sigue mostrando la versión antigua de la app (con LoginActivity como primera pantalla) aunque el código ya está cambiado.

## Solución: Desinstalar completamente y reinstalar

### PASO 1: Cerrar la app en el emulador
- Si la app está abierta, ciérrala completamente (botón Atrás varias veces o quitar de recientes).

### PASO 2: Desinstalar la app del emulador
**Opción A - Desde el launcher:**
1. En el emulador, busca el icono de **FilmBox** en la pantalla principal o en el cajón de apps.
2. **Mantén pulsado** el icono.
3. Arrastra hacia **"Desinstalar"** o toca el icono de desinstalar que aparece.

**Opción B - Desde Ajustes:**
1. Abre **Ajustes** en el emulador.
2. Ve a **Apps** o **Aplicaciones**.
3. Busca **FilmBox** o **filmbox-front**.
4. Toca en la app.
5. Toca **"Desinstalar"**.
6. Confirma **"Desinstalar"**.

### PASO 3: Verificar que se desinstaló
- Busca el icono de FilmBox en el emulador.
- **NO debe aparecer**. Si aparece, repite el paso 2.

### PASO 4: En Android Studio / Cursor
1. **Build → Clean Project** (espera a que termine completamente).
2. **Build → Rebuild Project** (espera a que termine completamente - puede tardar 1-2 minutos).

### PASO 5: Ejecutar la app de nuevo
1. **Run → Run 'app'** (o el botón verde de ejecutar).
2. Espera a que compile e instale.

### PASO 6: Verificar que es la versión nueva
Al abrir la app, **DEBE** aparecer un Toast que dice:
- **"App v1.3 - Pantalla de REGISTRO cargada"**

Si ves ese Toast, significa que:
- ✅ La nueva versión está instalada.
- ✅ La pantalla de REGISTRO es la primera pantalla.
- ✅ Todo está funcionando correctamente.

---

## Si después de desinstalar y reinstalar SIGUE mostrando Login

1. **Cierra completamente Android Studio / Cursor**.
2. **Cierra el emulador**.
3. **Abre de nuevo Android Studio / Cursor**.
4. **Inicia el emulador de nuevo**.
5. Repite los pasos 2-6 de arriba.

---

## Si NUNCA ves el Toast "Pantalla de REGISTRO cargada"

Significa que la app antigua sigue instalada. Prueba:

1. **Desde la terminal/consola** (en la carpeta del proyecto):
   ```bash
   adb uninstall com.example.filmbox_front
   ```
   Esto fuerza la desinstalación desde la línea de comandos.

2. Luego repite los pasos 4-6 de arriba.

---

## Resumen rápido
1. Desinstalar app del emulador.
2. Clean Project.
3. Rebuild Project.
4. Run app.
5. Verificar Toast "App v1.3 - Pantalla de REGISTRO cargada".
