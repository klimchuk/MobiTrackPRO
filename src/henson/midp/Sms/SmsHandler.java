/*
 * SmsHandler.java
 *
 * Created on 19 ���� 2005 �., 13:56
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
   * ������� ���������� �������� SMS ���������
   * @param msg ���������� ���������
   * @param data ������ ���� ������� � ���������
   */
  abstract public void Handler(Message msg, byte[] data);
  /**
   * ������� ���������� ��������� SMS ���������
   * @param msg ���������� ���������
   * @param text ����� ���������
   */
  abstract public void Handler(Message msg, String text);
  /**
   * ������� ���������� ������
   * @param s �������� ������
   */
  abstract public void Error(String s);
  /**
   * ��������� � ����������� ��������
   * @param s ����� ���������
   */
  abstract public void Info(String s);
}
