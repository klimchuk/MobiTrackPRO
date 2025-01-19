/*
 * SmsHandler.java
 *
 * Created on 19 Март 2005 г., 13:56
 */

package henson.midp.Sms;

import javax.wireless.messaging.*;

/**
 *
 * @author user
 */
public interface SmsHandler
{
  /**
   * Внешний обработчик бинарных SMS сообщений
   * @param msg полученное сообщение
   * @param data массив байт готовых к обработке
   */
  abstract public void Handler(Message msg, byte[] data);
  /**
   * Внешний обработчик текстовых SMS сообщений
   * @param msg полученное сообщение
   * @param text текст сообщения
   */
  abstract public void Handler(Message msg, String text);
  /**
   * Внешний обработчик ошибок
   * @param s описание ошибки
   */
  abstract public void Error(String s);
  /**
   * Сообщение о выполненной операции
   * @param s текст сообщения
   */
  abstract public void Info(String s);
}
