/* ============================================================
   MODULO DE BASE DE DATOS - Texas Hold'em
   ------------------------------------------------------------
   Este es el codigo EXACTO que usa el juego para guardar y leer
   datos. Usa localStorage, que es la base de datos que el propio
   navegador (Chrome, Firefox, Edge) guarda en el disco del usuario.

   CARACTERISTICAS:
   - No necesita instalar nada (no es MySQL, no es SQLite externo)
   - No necesita servidor ni conexion a internet
   - Los datos persisten aunque cierres el navegador o apagues la PC
   - Limite aproximado: 5-10 MB por sitio web (mas que suficiente
     para miles de rondas de poker)

   ESTRUCTURA: 2 "tablas" simuladas como claves de localStorage
   ------------------------------------------------------------
   Tabla 1) "poker_rounds"  -> Array JSON, una fila por ronda jugada
   Tabla 2) "poker_state"   -> Objeto JSON, una sola fila con el
                                estado actual del jugador (fichas y stats)
   ============================================================ */


// ----------------------------------------------------------
// DEFINICION DEL OBJETO DB
// Agrupamos todas las funciones de base de datos en un solo
// objeto llamado "DB" para mantener el codigo organizado,
// similar a como se usa una clase con metodos en Python.
// ----------------------------------------------------------
const DB = {

  // Nombres de las "tablas" (claves dentro de localStorage)
  ROUNDS_KEY: 'poker_rounds',
  STATE_KEY: 'poker_state',


  // ==========================================================
  // INIT — Inicializa la base de datos la primera vez que se abre
  // ==========================================================
  // Si las claves no existen todavia (primera vez que el usuario
  // abre el juego), las crea vacias con valores por defecto.
  init(){
    try{
      // Si no existe el estado del jugador, lo crea con valores iniciales
      if(!localStorage.getItem(DB.STATE_KEY)){
        localStorage.setItem(DB.STATE_KEY, JSON.stringify(defaultState()));
      }
      // Si no existe la tabla de rondas, la crea como un array vacio
      if(!localStorage.getItem(DB.ROUNDS_KEY)){
        localStorage.setItem(DB.ROUNDS_KEY, JSON.stringify([]));
      }
      setDbStatus(true); // muestra el punto verde de "conectado"
      return true;
    }catch(e){
      // Esto puede fallar en modo incognito muy restrictivo,
      // o si el usuario desactivo el almacenamiento del navegador
      setDbStatus(false); // muestra el punto rojo de "sin conexion"
      return false;
    }
  },


  // ==========================================================
  // INSERT — Guarda una ronda nueva en la tabla "poker_rounds"
  // ==========================================================
  // Recibe un objeto "row" con los datos de la ronda que termino
  // (cartas, mano, resultado, fichas) y lo agrega al final del
  // arreglo guardado.
  saveRound(row){
    try{
      // 1. Leemos las rondas que ya existian (si no hay, usamos [])
      const rows = JSON.parse(localStorage.getItem(DB.ROUNDS_KEY) || '[]');

      // 2. Le agregamos un identificador unico y una fecha legible
      row.id = Date.now();                            // numero unico basado en el tiempo actual
      row.ts = new Date().toLocaleString('es-PE');     // fecha y hora en formato peruano

      // 3. Agregamos la nueva fila al final del arreglo
      rows.push(row);

      // 4. Limitamos el historial a 200 filas como maximo,
      //    para no llenar el espacio de almacenamiento del navegador
      const trimmed = rows.length > 200 ? rows.slice(-200) : rows;

      // 5. Guardamos el arreglo actualizado de vuelta en localStorage
      //    (siempre como texto JSON, porque localStorage solo
      //     guarda strings, nunca objetos directamente)
      localStorage.setItem(DB.ROUNDS_KEY, JSON.stringify(trimmed));

    }catch(e){
      console.warn('Error guardando ronda en la base de datos:', e);
    }
  },


  // ==========================================================
  // SELECT * — Devuelve TODAS las rondas guardadas
  // ==========================================================
  getRounds(){
    try{
      // JSON.parse convierte el texto guardado de vuelta en un array de JS
      return JSON.parse(localStorage.getItem(DB.ROUNDS_KEY) || '[]');
    }catch(e){
      return []; // si algo falla, devolvemos un arreglo vacio en vez de romper el juego
    }
  },


  // ==========================================================
  // UPDATE — Sobrescribe el estado actual del jugador
  // ==========================================================
  // Se llama cada vez que cambian las fichas o las estadisticas
  // (despues de cada ronda, o al reiniciar el juego).
  saveState(state){
    try{
      localStorage.setItem(DB.STATE_KEY, JSON.stringify(state));
    }catch(e){
      console.warn('Error guardando el estado del jugador:', e);
    }
  },


  // ==========================================================
  // SELECT — Carga el estado guardado del jugador
  // ==========================================================
  // Se llama una sola vez, al abrir el juego, para recuperar
  // las fichas y estadisticas de la ultima vez que se jugo.
  loadState(){
    try{
      const saved = localStorage.getItem(DB.STATE_KEY);
      return saved ? JSON.parse(saved) : defaultState();
    }catch(e){
      return defaultState(); // si hay error, empezamos de cero
    }
  },


  // ==========================================================
  // DELETE — Borra todo el historial de rondas
  // ==========================================================
  // No borra el estado de fichas, solo el historial de partidas.
  clearRounds(){
    localStorage.setItem(DB.ROUNDS_KEY, JSON.stringify([]));
  }

};


// ----------------------------------------------------------
// Valores iniciales para un jugador nuevo (sin historial previo)
// ----------------------------------------------------------
function defaultState(){
  return {
    playerChips: 1000,   // fichas del jugador humano
    cpuChips: 1000,       // fichas de la CPU
    wins: 0,               // rondas ganadas
    losses: 0,             // rondas perdidas
    ties: 0,                // rondas empatadas
    streak: 0,              // racha actual (positiva=ganando, negativa=perdiendo)
    bestStreak: 0,           // mejor racha histórica
    profit: 0,                // ganancia/perdida neta acumulada
    round: 0                   // numero de la ultima ronda jugada
  };
}


// ----------------------------------------------------------
// Actualiza el indicador visual de "conectado / desconectado"
// ----------------------------------------------------------
function setDbStatus(ok){
  document.getElementById('dbDot').className = 'db-dot' + (ok ? '' : ' off');
  document.getElementById('dbLbl').textContent = ok
    ? 'DB conectada (localStorage)'
    : 'Sin almacenamiento disponible';
}


/* ============================================================
   EJEMPLO DE USO (esto es lo que hace el juego automaticamente)
   ============================================================

   // Al abrir el juego:
   DB.init();
   const estadoGuardado = DB.loadState();
   console.log(estadoGuardado);
   // -> { playerChips: 850, cpuChips: 1150, wins: 3, losses: 2, ... }

   // Al terminar una ronda:
   DB.saveRound({
     round: 6,
     playerCards: "A♠ K♥",
     community: "Q♦ J♣ 10♠ 9♥ 2♦",
     playerHand: "Escalera",
     cpuHand: "Par",
     resultado: "Gano",
     delta: 120,
     fichas: 970
   });

   DB.saveState({
     playerChips: 970, cpuChips: 1030,
     wins: 4, losses: 2, ties: 0,
     streak: 1, bestStreak: 2, profit: 70, round: 6
   });

   // Para mostrar el historial completo:
   const todasLasRondas = DB.getRounds();
   todasLasRondas.forEach(r => console.log(r));

   // Para borrar todo el historial:
   DB.clearRounds();

   ============================================================ */
