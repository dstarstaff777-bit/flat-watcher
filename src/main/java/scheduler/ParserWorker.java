package scheduler;


import bot.MyWebhookBot;
import db.Database;
import parser.AvitoParser;
import model.Flat;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ParserWorker — это фоновый планировщик.
 *
 * Он каждые N минут запускает Selenium-парсер Avito, получает список квартир,
 * проверяет наличие новых объявлений, сохраняет их в PostgreSQL
 * и отправляет сообщения через Telegram Бота.
 */
public class ParserWorker {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private final AvitoParser parser;
    private final MyWebhookBot bot;

    // Интервал между запусками парсера (в минутах)
    private final int intervalMinutes;

    public ParserWorker(AvitoParser parser, MyWebhookBot bot, int intervalMinutes) {
        this.parser = parser;
        this.bot = bot;
        this.intervalMinutes = intervalMinutes;
    }

    /**
     * Запускает выполнение задачи по расписанию.
     */
    public void start() {
        System.out.println("[ParserWorker] Фоновый парсер запущен. Интервал: " + intervalMinutes + " минут.");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[ParserWorker] Запуск парсинга Avito...");

                // Загружаем список объявлений
                List<Flat> flats = parser.loadFlats();

                System.out.println("[ParserWorker] Найдено объявлений: " + flats.size());

                for (Flat f : flats) {

                    // Проверка, новое ли объявление
                    if (Database.isNew(f.id)) {

                        // Сохраняем новую запись в БД
                        Database.saveFlat(f);

                        // Формируем текст уведомления
                        String msg = "? <b>Новая квартира на Avito!</b>\n\n" +
                                "<b>" + f.title + "</b>\n" +
                                "? Цена: " + f.price + "\n" +
                                "? <a href=\"" + f.url + "\">Открыть объявление</a>";

                        // Отправляем пользователям
                        bot.broadcast(msg);

                        System.out.println("[ParserWorker] Новое объявление: " + f.id);
                    }
                }

                System.out.println("[ParserWorker] Парсинг завершён.");

            } catch (Exception e) {
                System.out.println("[ParserWorker] Ошибка при выполнении: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, intervalMinutes, TimeUnit.MINUTES);
    }
}