/*
 * SmsManager.java
 *
 * Created on 19 ���� 2005 �., 13:57
 */

package henson.midp.Sms;

import javax.microedition.io.Connector;
import javax.wireless.messaging.*;

/**
 *
 * @author user
 */
public class SmsManager implements Runnable , MessageListener
{
  private SmsHandler handler=null;
  private MessageConnection conn=null;
  private BinaryMessage binMessage=null;
  private TextMessage txtMessage=null;
  /**
   * �����������
   * @param handler ��������� ��� ������� ��������� SMS
   */
  public SmsManager(SmsHandler handler)
  {
    this.handler=handler;
  }
  /**
   * ������� ���������� �� ����� ��� ������ SMS ���������
   * @param port ����� �����
   * @return true � ������ ������ � false � ������ ������
   */
  public boolean listen(int port)
  {
    if(handler==null)
      return false;
    //
    if(!close())
      return false;
    //
    try
    {
      conn = (MessageConnection)Connector.open("sms://:"+port);
      conn.setMessageListener(this);
    }
    catch (Exception ex)
    {
      if(ex instanceof SecurityException)
        handler.Error("According security settings of midlet any of incoming SMSs will not be handled by the MobiTrackPRO.");
      else
        handler.Error("SmsManager::listen\n"+ex.toString());
      //ex.printStackTrace();
      return false;
    }
    //
    return true;
  }
  /**
   * ���������� ����������� SMS ���������
   * @param conn ������ ������ MessageConnection
   */
  public void notifyIncomingMessage(MessageConnection conn)
  {
    binMessage=null;
    txtMessage=null;
    new Thread(this).start();
  }
  /**
   * �������� ���������� SMS ���������
   * @param address ����� ����������
   * @param port ���� ����������
   * @param text ����� ���������
   * @return true � ������ ������ � false � ������ ������
   */
  public boolean sendText(String address, int port, String text)
  {
    if(!close())
      return false;
    //
    try
    {
        if(port==0)
            conn = (MessageConnection)Connector.open("sms://"+address);
        else
            conn = (MessageConnection)Connector.open("sms://"+address+":"+port);
      txtMessage=(TextMessage)conn.newMessage(MessageConnection.TEXT_MESSAGE);
      txtMessage.setPayloadText(text);
      new Thread(this).start();
    }
    catch (Exception ex)
    {
      handler.Error("SmsManager::sendText\n"+ex.toString());
      ex.printStackTrace();
      return false;
    }
    //
    return true;
  }
  /**
   * ��������� SMS ��������� � ��������� �������
   * @param address ����� ����������, �������� +79262807121
   * @param port ���� ����������
   * @param data ������ ����
   * @return true � ������ ������ � false � ������ ������
   */
  public boolean send(String address, int port, byte[] data)
  {
    if(!close())
      return false;
    //
    try
    {
      conn = (MessageConnection)Connector.open("sms://"+address+":"+port);
      binMessage=(BinaryMessage)conn.newMessage(MessageConnection.BINARY_MESSAGE);
      binMessage.setPayloadData(data);
      new Thread(this).start();
    }
    catch (Exception ex)
    {
      handler.Error("SmsManager::send\n"+ex.toString());
      ex.printStackTrace();
      return false;
    }
    //
    return true;
  }
  /**
   * ������� ����������
   * @return true � ������ ������ � false � ������ ������
   */
  public boolean close()
  {
    if(conn!=null)
    {
      try
      {
        conn.close();
        conn=null;
      }
      catch (Exception ex)
      {
        handler.Error("SmsManager::close\n"+ex.toString());
        ex.printStackTrace();
        return false;
      }
    }
    //
    return true;
  }
  /**
   * ��������� ����� ��� �������� ��� ��������� ����������� ���������
   */
  public void run()
  {
    try
    {
      if(binMessage!=null)
      {
        conn.send(binMessage);
        handler.Info("SMS was successfully sent ["+binMessage.getPayloadData().length+"]");
        binMessage=null;
      }
      else
      if(txtMessage!=null)
      {
        conn.send(txtMessage);
        handler.Info("SMS was successfully sent: "+txtMessage.getPayloadText());
        txtMessage=null;
      }
      else
      {
        if(conn==null)
        {
          handler.Error("SMS connection error");
        }
        else
        {
          Message msg = conn.receive();
          if (msg != null)
          {
            if(msg instanceof BinaryMessage)
            {
              BinaryMessage binMsg = (BinaryMessage) msg;
              byte[] data = binMsg.getPayloadData();
              if(data!=null && data.length>3)
                handler.Handler(binMsg, data);
              else
                handler.Error("Empty message");
            }
            else
            if(msg instanceof TextMessage)
            {
              TextMessage txtMsg = (TextMessage) msg;
              String str=txtMsg.getPayloadText();
              if(str!=null && str.length()>0)
                handler.Handler(txtMsg, str);
              else
                handler.Error("Empty message");
            }
            else
              handler.Error("Wrong format");
          }
          else
            handler.Error("Message not found");
        }
      }
    }
    catch (Exception ex)
    {
      handler.Error("SmsManager::run\n"+ex.toString());
      ex.printStackTrace();
      //close();
    }
  }
}
