package academy.backend.market_pulse.demo;

import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/** Демо для демонстрации различных подходов к функциональному программированию
 * при использовании vavr. Демонстрирует разные подходы к обработке checked исключений
 */
public class CheckedExceptionVsVavrDemo {

    /**
     * Функция, которая можем выбросить checked исключение
     */
    public static InputStream openFileAsStream(String fileName) throws IOException {
        return new FileInputStream(fileName);
    }


    public static void main(String[] args) {
        final String fileName = "Имя файла";

        // способ использования функции в интерфейсе Function
        // для дальнейшего использования в функциональном пайплайне
        Supplier<InputStream> function = () -> {
            try {
                return openFileAsStream(fileName);
            } catch (IOException e) {
                /* при обработке исключения нам нужно вернуть какое-то значение
                   или обернуть исключение в RuntimeException и в дальнейшем обработать его
                   для непрерывности пайплайна
                 */
                return null;
                //throw new RuntimeException(e);
            }
        };

        System.out.println("supplier result = " + function.get());

        // способ использования функции в интерфейсе CheckedFunction0, используемом в Try.of
        CheckedFunction0<InputStream> checkedFunction = () -> openFileAsStream(fileName);

        /* В случае с Try обработка исключения может быть встроена в пайплайн позже
           или раньше (как в текущем примере)
         */
        Try<InputStream> tryFunction = Try.of(checkedFunction)
                .recover(IOException.class, (InputStream) null);

        System.out.println("supplier result = " + tryFunction.get());


    }



}
