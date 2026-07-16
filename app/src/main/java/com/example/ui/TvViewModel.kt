package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class VoiceParseResult(
    val action: String,
    val parameter: String? = null,
    val reason: String
)

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val message: String) : UiState
    data class Error(val error: String) : UiState
}

class TvViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TvDatabase.getDatabase(application)
    private val repository = TvRepository(database.tvDao())

    // --- State Flows ---
    val allDevices = repository.allDevices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lastConnectedDevice = repository.lastConnectedDevice.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val history = repository.history.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allMacros = repository.allMacros.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium = _isPremium.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _aiThinking = MutableStateFlow(false)
    val aiThinking = _aiThinking.asStateFlow()

    private val _lastAiResult = MutableStateFlow<VoiceParseResult?>(null)
    val lastAiResult = _lastAiResult.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _showPairingDialog = MutableStateFlow<TvDevice?>(null)
    val showPairingDialog = _showPairingDialog.asStateFlow()

    private val _touchpadLog = MutableStateFlow("Touchpad ativo...")
    val touchpadLog = _touchpadLog.asStateFlow()

    init {
        // Populate default TVs if empty and reconnect last active
        viewModelScope.launch {
            allDevices.first() // Wait for initial loading
            if (allDevices.value.isEmpty()) {
                val defaultTvs = listOf(
                    TvDevice(name = "Samsung Sala", brand = "Samsung", ipAddress = "192.168.1.15", isFavorite = true),
                    TvDevice(name = "LG Quarto", brand = "LG", ipAddress = "192.168.1.18", isFavorite = true),
                    TvDevice(name = "Sony Bravia Escritório", brand = "Sony", ipAddress = "192.168.1.22", isFavorite = false)
                )
                for (tv in defaultTvs) {
                    repository.insertDevice(tv)
                }
            }
            if (allMacros.value.isEmpty()) {
                val defaultMacros = listOf(
                    Macro(name = "Cinema em Casa", commands = "POWER,OPEN_NETFLIX,VOLUME_UP,VOLUME_UP", isFavorite = true),
                    Macro(name = "Modo de Música", commands = "POWER,OPEN_SPOTIFY,VOLUME_UP", isFavorite = false)
                )
                for (macro in defaultMacros) {
                    repository.addMacro(macro)
                }
            }
        }
    }

    // --- Authentication Actions ---
    fun login(email: String, password: String): Boolean {
        if (email.isNotBlank() && password.length >= 6) {
            _isLoggedIn.value = true
            _userEmail.value = email
            return true
        }
        return false
    }

    fun register(email: String, password: String): Boolean {
        if (email.contains("@") && password.length >= 6) {
            _isLoggedIn.value = true
            _userEmail.value = email
            return true
        }
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
        _userEmail.value = ""
    }

    // --- Premium Activation ---
    fun setPremium(premium: Boolean) {
        _isPremium.value = premium
    }

    // --- Scanner Actions ---
    fun startScanning() {
        if (_isScanning.value) return
        _isScanning.value = true
        viewModelScope.launch {
            delay(2500) // Simulate scanning
            _isScanning.value = false
        }
    }

    fun addCustomDevice(name: String, brand: String, ipAddress: String, connectionType: String) {
        viewModelScope.launch {
            val cleanIp = if (ipAddress.isBlank()) "192.168.1.${(10..254).random()}" else ipAddress
            repository.insertDevice(
                TvDevice(name = name, brand = brand, ipAddress = cleanIp, connectionType = connectionType)
            )
        }
    }

    fun toggleFavoriteDevice(device: TvDevice) {
        viewModelScope.launch {
            repository.setFavorite(device.id, !device.isFavorite)
        }
    }

    fun deleteDevice(device: TvDevice) {
        viewModelScope.launch {
            if (lastConnectedDevice.value?.id == device.id) {
                repository.disconnectDevice()
            }
            repository.deleteDevice(device)
        }
    }

    // --- Connection Handling ---
    fun connectToTv(device: TvDevice) {
        _showPairingDialog.value = device
    }

    fun confirmPairing(pairingCode: String) {
        val device = _showPairingDialog.value ?: return
        viewModelScope.launch {
            repository.connectToDevice(device.id)
            repository.addHistory(device.name, "Conexão Estabelecida (Pareamento via Wi-Fi)")
            _showPairingDialog.value = null
        }
    }

    fun cancelPairing() {
        _showPairingDialog.value = null
    }

    fun disconnectTv() {
        viewModelScope.launch {
            lastConnectedDevice.value?.let {
                repository.addHistory(it.name, "Desconectado")
            }
            repository.disconnectDevice()
        }
    }

    // --- Remote Control Button Actions ---
    fun sendCommand(command: String) {
        val device = lastConnectedDevice.value
        viewModelScope.launch {
            if (device != null) {
                repository.addHistory(device.name, command)
            } else {
                // If no TV connected, log to a generic historical log
                repository.addHistory("Controle Sem TV", command)
            }
        }
    }

    // --- Macro Actions ---
    fun addMacro(name: String, commands: String) {
        viewModelScope.launch {
            repository.addMacro(Macro(name = name, commands = commands))
        }
    }

    fun deleteMacro(macro: Macro) {
        viewModelScope.launch {
            repository.deleteMacro(macro)
        }
    }

    fun runMacro(macro: Macro) {
        viewModelScope.launch {
            val device = lastConnectedDevice.value
            val deviceName = device?.name ?: "Nenhuma TV Conectada"
            repository.addHistory(deviceName, "Executando Macro: ${macro.name}")
            
            val cmds = macro.commands.split(",")
            for (cmd in cmds) {
                val cleanCmd = cmd.trim()
                if (cleanCmd.isNotBlank()) {
                    repository.addHistory(deviceName, cleanCmd)
                    delay(800) // Delay between commands in a macro to simulate infrared/network signals
                }
            }
        }
    }

    // --- Touchpad Gestures ---
    fun registerTouchpadMovement(direction: String) {
        _touchpadLog.value = "Gesto detectado: deslizar para $direction"
        sendCommand("TOUCHPAD_SWIPE_$direction")
    }

    fun registerTouchpadClick() {
        _touchpadLog.value = "Toque detectado: Confirmar (OK)"
        sendCommand("OK")
    }

    // --- Clear History ---
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- AI / Voice Commands ---
    fun processVoiceCommand(text: String) {
        if (text.isBlank()) return
        _aiThinking.value = true
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val key = BuildConfig.GEMINI_API_KEY
            val isMockKey = key.isEmpty() || key == "MY_GEMINI_API_KEY"

            if (isMockKey) {
                // FALLBACK: Highly precise offline parser to ensure flawless functionality
                delay(1200) // Simulate networking delay
                val result = parseCommandOffline(text)
                withContext(Dispatchers.Main) {
                    _lastAiResult.value = result
                    _aiThinking.value = false
                    _uiState.value = UiState.Success("IA offline processou o comando")
                    executeParsedAction(result)
                }
            } else {
                // REAL GEMINI CALL
                try {
                    val prompt = """
                        Interprete o seguinte comando em português para um controle remoto de Smart TV: "$text".
                        Mapeie-o para uma das seguintes ações suportadas:
                        POWER (ligar/desligar)
                        VOLUME_UP (aumentar volume)
                        VOLUME_DOWN (diminuir volume)
                        MUTE (mudo)
                        CHANNEL_UP (próximo canal)
                        CHANNEL_DOWN (canal anterior)
                        HOME (tela inicial)
                        BACK (voltar)
                        MENU (configurações/menu)
                        INPUT_HDMI_1 (entrada hdmi 1)
                        INPUT_HDMI_2 (entrada hdmi 2)
                        INPUT_HDMI_3 (entrada hdmi 3)
                        OK (confirmar)
                        UP (seta para cima)
                        DOWN (seta para baixo)
                        LEFT (seta para esquerda)
                        RIGHT (seta para direita)
                        OPEN_NETFLIX (abrir netflix)
                        OPEN_PRIME_VIDEO (abrir prime video)
                        OPEN_DISNEY (abrir disney+)
                        OPEN_YOUTUBE (abrir youtube)
                        OPEN_MAX (abrir hbo max)
                        OPEN_SPOTIFY (abrir spotify)
                        OPEN_GLOBOPLAY (abrir globoplay)
                        OPEN_CRUNCHYROLL (abrir crunchyroll)
                        SEARCH (pesquisa livre)

                        Se o comando não corresponder diretamente, use SEARCH e coloque o termo principal no campo parameter.

                        Retorne APENAS um objeto JSON válido seguindo estritamente este formato:
                        {
                          "action": "ACTION_NAME",
                          "parameter": "valor opcional se aplicável",
                          "reason": "Explicação curta e amigável em português sobre a ação executada"
                        }
                    """.trimIndent()

                    val request = GeminiRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                        generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.2f)
                    )

                    val response = GeminiRetrofitClient.api.generateContent(key, request)
                    val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                    if (rawJson != null) {
                        // Parse JSON using Moshi
                        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                        val adapter = moshi.adapter(VoiceParseResult::class.java)
                        val parsed = adapter.fromJson(rawJson)
                        
                        withContext(Dispatchers.Main) {
                            if (parsed != null) {
                                _lastAiResult.value = parsed
                                _aiThinking.value = false
                                _uiState.value = UiState.Success("Comando processado com Inteligência Artificial")
                                executeParsedAction(parsed)
                            } else {
                                throw Exception("Falha ao analisar a resposta JSON da IA")
                            }
                        }
                    } else {
                        throw Exception("Resposta vazia da IA")
                    }

                } catch (e: Exception) {
                    Log.e("TvViewModel", "Gemini call error", e)
                    // Fallback to offline on actual Gemini error
                    val result = parseCommandOffline(text)
                    withContext(Dispatchers.Main) {
                        _lastAiResult.value = result.copy(reason = "IA Online indisponível. Fallback inteligente: ${result.reason}")
                        _aiThinking.value = false
                        _uiState.value = UiState.Success("Comando processado por inteligência local")
                        executeParsedAction(result)
                    }
                }
            }
        }
    }

    private fun executeParsedAction(result: VoiceParseResult) {
        val cmdString = if (result.parameter != null) {
            "${result.action}:${result.parameter}"
        } else {
            result.action
        }
        sendCommand(cmdString)
    }

    private fun parseCommandOffline(text: String): VoiceParseResult {
        val normalized = text.lowercase()
        return when {
            normalized.contains("ligar") || normalized.contains("desligar") || normalized.contains("power") -> 
                VoiceParseResult("POWER", null, "Alternando o estado de energia da TV (Liga/Desliga).")
            normalized.contains("netflix") -> 
                VoiceParseResult("OPEN_NETFLIX", null, "Iniciando o aplicativo Netflix na Smart TV.")
            normalized.contains("youtube") -> 
                VoiceParseResult("OPEN_YOUTUBE", null, "Abrindo o YouTube para assistir a vídeos.")
            normalized.contains("prime") || normalized.contains("amazon") -> 
                VoiceParseResult("OPEN_PRIME_VIDEO", null, "Iniciando o Prime Video.")
            normalized.contains("disney") -> 
                VoiceParseResult("OPEN_DISNEY", null, "Carregando o Disney+.")
            normalized.contains("max") || normalized.contains("hbo") -> 
                VoiceParseResult("OPEN_MAX", null, "Abrindo o aplicativo Max.")
            normalized.contains("spotify") -> 
                VoiceParseResult("OPEN_SPOTIFY", null, "Iniciando o Spotify para reproduzir suas músicas.")
            normalized.contains("globoplay") -> 
                VoiceParseResult("OPEN_GLOBOPLAY", null, "Carregando o Globoplay.")
            normalized.contains("aumentar volume") || normalized.contains("volume +") || normalized.contains("mais volume") -> 
                VoiceParseResult("VOLUME_UP", null, "Aumentando o volume da TV.")
            normalized.contains("diminuir volume") || normalized.contains("volume -") || normalized.contains("menos volume") -> 
                VoiceParseResult("VOLUME_DOWN", null, "Diminuindo o volume da TV.")
            normalized.contains("mudo") || normalized.contains("silenciar") -> 
                VoiceParseResult("MUTE", null, "Alternando modo silencioso (Mudo).")
            normalized.contains("canal anterior") || normalized.contains("canal -") || normalized.contains("voltar canal") -> 
                VoiceParseResult("CHANNEL_DOWN", null, "Mudando para o canal anterior.")
            normalized.contains("canal") || normalized.contains("próximo canal") || normalized.contains("canal +") -> 
                VoiceParseResult("CHANNEL_UP", null, "Mudando para o próximo canal.")
            normalized.contains("hdmi 1") -> 
                VoiceParseResult("INPUT_HDMI_1", null, "Alterando a entrada da TV para HDMI 1.")
            normalized.contains("hdmi 2") -> 
                VoiceParseResult("INPUT_HDMI_2", null, "Alterando a entrada da TV para HDMI 2.")
            normalized.contains("hdmi 3") -> 
                VoiceParseResult("INPUT_HDMI_3", null, "Alterando a entrada da TV para HDMI 3.")
            normalized.contains("voltar") -> 
                VoiceParseResult("BACK", null, "Retornando para a tela anterior.")
            normalized.contains("home") || normalized.contains("inicio") || normalized.contains("início") -> 
                VoiceParseResult("HOME", null, "Retornando para a tela inicial da TV.")
            normalized.contains("menu") || normalized.contains("ajustes") || normalized.contains("configurações") -> 
                VoiceParseResult("MENU", null, "Abrindo as configurações da Smart TV.")
            normalized.contains("pesquisar") || normalized.contains("buscar") -> {
                val term = text.replace("pesquisar", "").replace("buscar", "").trim()
                VoiceParseResult("SEARCH", term, "Pesquisando por: \"$term\" na TV.")
            }
            else -> VoiceParseResult("SEARCH", text, "Pesquisando inteligente por \"$text\".")
        }
    }
}
