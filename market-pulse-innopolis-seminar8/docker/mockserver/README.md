# Моковый instruments-service (Docker)

Имитирует ручку `GET /v1/instruments` T-Invest API для семинара 7, продвинутый трек (см. «План
семинара.md» в `advanced/docs/seminars/seminar7/`). Поведение переключается заголовком
`X-Mock-Scenario`. Сценарий по умолчанию отвечает с задержкой **300 мс** — специально не мгновенно,
иначе на этапах 3/7 не будет видно разницы между блокирующим клиентом и реактором.

## Запуск

```bash
docker compose -f docker/mockserver/docker-compose.yml up -d
```

Поднимется на `http://localhost:1080` — адрес, который подставлен по умолчанию в
`TInvestApiProperties.localMock()`.

## Проверка

```bash
curl http://localhost:1080/v1/instruments                                   # ~300 мс, 6 инструментов
curl -H "X-Mock-Scenario: error" http://localhost:1080/v1/instruments       # мгновенно, 500
curl -H "X-Mock-Scenario: timeout" http://localhost:1080/v1/instruments     # 10 секунд, затем []
```

## Сбой из приложения

Клиенты приложения сами отправляют `X-Mock-Scenario`, если задана переменная окружения
`MARKET_PULSE_MOCK_SCENARIO` (`error` | `timeout` | `ok`; пусто — заголовок не отправляется):

```bash
MARKET_PULSE_MOCK_SCENARIO=error java ... academy.backend.market_pulse.demo.Main update
```

Перед `update` в watchlist должен быть хотя бы один тикер (`watch SBER`).

## Остановка

```bash
docker compose -f docker/mockserver/docker-compose.yml down
```

## Важно для семинариста

Поднять и прогнать все три `curl`-команды выше, а также
`demo.BlockingClientDemo`/`demo.TInvestApiClientDemo`/`benchmark.ClientThroughputBenchmark` —
**заранее**, на той машине, с которой будете вести занятие. Этот `docker-compose.yml` и
`expectations.json` не запускались и не проверялись при подготовке (в среде, где готовился этот
код, Docker-демон оказался нестабилен и падал при `docker compose up`) — перед использованием
убедитесь, что `mockserver/mockserver:5.15.0` — актуальный тег образа и что формат
`expectations.json` (ключ `delay`, вложенный `body.json`) совпадает с версией MockServer, которую вы
фактически поднимете. Корректность самого клиентского кода (и блокирующего, и реакторного)
проверена тестами на встроенном `com.sun.net.httpserver.HttpServer`, без Docker — это не заменяет
проверку реального мока, только снимает сомнения в правильности логики клиентов.
