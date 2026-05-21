<div align="center">
  <img src="app/src/main/res/drawable/app_icon.png" width="150" height="150" alt="VPN Inspector Logo"/>
  
  <h1>🛡️ VPN Inspector</h1>
  <p>
    <b>Анализатор обходов блокировок: демонстрация методов обнаружения VPN и Proxy на мобильных устройствах.</b>
  </p>

  <p>
    <a href="https://android.com"><img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" /></a>
    <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
    <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Compose" />
    <img src="https://img.shields.io/badge/Retrofit-FF5722?style=for-the-badge&logo=appveyor&logoColor=white" alt="Retrofit" />
  </p>
</div>

---

## 📖 О проекте

**VPN Inspector** — это специализированное Android-приложение, созданное для демонстрации и глубокого анализа методов обнаружения средств обхода блокировок. Оно реализует современные проверки, основанные на официально утвержденной методике выявления VPN и Proxy-серверов. 

Главная цель приложения — наглядно показать, как именно различные сервисы могут определять факт использования туннелей или прокси, и доказать, что для надежного сокрытия трафика необходим комплексный подход.

## 🎯 Для кого этот проект?

* 👨‍💻 **Разработчики и тестировщики**, интересующиеся сетевой безопасностью и механизмами туннелирования.
* 🔐 **Пользователи**, желающие проверить надежность своего VPN-подключения или Proxy-сервера.
* 🎓 **IT-энтузиасты**, изучающие механизмы работы Android API с сетевыми интерфейсами.

## 🔬 Что проверяет приложение?

Приложение проводит многоуровневый анализ, разделенный на категории риска:

* 🌍 **GeoIP (Анализ на стороне сервера)** — сравнение вашего провайдера и IP-адреса с репутационными базами (проверка на хостинг, прокси и нецелевой регион).
* ⚙️ **Системный VPN API** — прямой опрос `NetworkCapabilities` на наличие активного флага VPN.
* 🖧 **Обнаружение интерфейсов** — поиск виртуальных адаптеров туннелирования (например, `tun`, `tap`, `wg`).
* 📦 **Анализ MTU** — выявление аномально низкого размера сетевых пакетов (MTU), характерного для инкапсуляции при использовании VPN.
* 🛡️ **Локальные Proxy** — сканирование открытых портов (например, `1080`, `10808`), используемых популярными proxy-клиентами.
* 🎭 **Подмена IP (Fake-IP)** — проверка выдачи фейковых локальных IP-адресов популярным серверам.
* 📱 **Установленные пакеты** — сканирование установленных на устройстве известных VPN-клиентов (WireGuard, ProtonVPN, v2rayNG, Shadowsocks и др.).

> **💡 Методика:** В приложение встроен раздел **"Методичка"**, содержащий исчерпывающую теоретическую базу по каждому из методов обнаружения.

## 🛠 Технологический стек

* **Язык разработки:** Kotlin
* **Интерфейс:** Jetpack Compose, Material Design 3 (Светлая и тёмная темы)
* **Сеть/API:** Retrofit, Moshi
* **Асинхронность:** Kotlin Coroutines & Flow
* **Изображения:** Coil

## 🚀 Как запустить и собрать

Приложение можно собрать и протестировать с помощью [Android Studio](https://developer.android.com/studio).

```bash
# 1. Клонируйте репозиторий
git clone https://github.com/Kasumicic/vpn-inspector.git
cd vpn-inspector

# 2. Соберите проект через Gradle
./gradlew assembleDebug
```
Готовый APK файл вы сможете найти в директории `app/build/outputs/apk/debug/`.

---

<div align="center">
  <img src="https://github.com/Kasumicic.png" width="60" style="border-radius:50%" />
  <br>
  Разработано с ❤️ <b><a href="https://github.com/Kasumicic">Kasumicic</a></b>
</div>
