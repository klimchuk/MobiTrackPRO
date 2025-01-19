package henson.midp;

import javax.microedition.lcdui.*;

public class CommandManager
{
  public static Command helpCommand=new Command("Help", Command.ITEM, 1);
  public static Command deleteCommand=new Command("Delete", Command.ITEM, 1);
  public static Command deleteAllCommand=new Command("Delete All", Command.ITEM, 1);
  public static Command refreshCommand=new Command("Refresh", Command.OK, 1);
  public static Command aboutCommand=new Command("About", Command.ITEM, 1);
  public static Command exitCommand=new Command("Exit", Command.EXIT, 1);
  public static Command disconnectCommand=new Command("Disconnect", Command.BACK, 1);
  public static Command backCommand=new Command("Back", Command.BACK, 1);
  public static Command placesCommand=new Command("Locations", Command.ITEM, 1);
  public static Command editCommand=new Command("Edit", Command.ITEM, 1);
  public static Command okCommand=new Command("OK", Command.OK, 1);
  public static Command cancelCommand=new Command("Cancel", Command.CANCEL, 1);
  //public static Command networkCommand=new Command("Network", Command.OK, 1);
  public static Command settingsCommand=new Command("Settings >", Command.OK, 1);
  public static Command findCommand=new Command("Select location", Command.ITEM, 1);
  public static Command closeCommand=new Command("Close", Command.CANCEL, 1);
  public static Command removeCommand=new Command("Remove", Command.ITEM, 1);
  public static Command noCommand=new Command("No", Command.CANCEL, 1);
  public static Command yesCommand=new Command("Yes", Command.OK, 1);
  //public static Command markPointCommand=new Command("Mark point", Command.OK, 1);
  public static Command selectPointCommand=new Command("Waypoints >", Command.OK, 1);
  public static Command clearCommand=new Command("Clear >", Command.OK, 1);
  //public static Command clearAllCommand=new Command("All", Command.OK, 1);
  //public static Command clearTrackCommand=new Command("Clear track", Command.OK, 1);
  public static Command sendTrackCommand=new Command("Send track [gprs]", Command.OK, 1);
  public static Command sendTrackCommand2=new Command("Send track [bt]", Command.OK, 1);
  public static Command sendKMLCommand=new Command("Send KML [bt]", Command.OK, 1);
  public static Command sendWaypointsCommand=new Command("Send waypoints [bt]", Command.OK, 1);
  //public static Command resetTripCommand=new Command("Reset trip", Command.OK, 1);
  //public static Command resetOdometerCommand=new Command("Reset odometer", Command.OK, 1);
  public static Command selectMapCommand=new Command("Select map >", Command.OK, 1);
  public static Command appendCommand=new Command("Append", Command.OK, 1);
  public static Command regCommand=new Command("Registration", Command.OK, 1);
  //public static Command offlineCommand=new Command("Offline mode", Command.OK, 1);
  //public static Command demoCommand=new Command("Demo mode", Command.OK, 1);
  public static Command unselectCommand=new Command("Unselect", Command.OK, 1);
  //public static Command serialCommand=new Command("Serial connection", Command.OK, 1);
  public static Command sendCommand=new Command("Send", Command.OK, 1);
  public static Command receiveCommand=new Command("Receive", Command.OK, 1);
  public static Command sendScreenshotCommand=new Command("Send last screenshot [bt]", Command.OK, 1);
  public static Command routeCommand=new Command("Route >", Command.OK, 1);
  public static Command reverseCommand=new Command("Reverse route", Command.OK, 1);
  public static Command insertWptBeforeCommand=new Command("Insert before", Command.OK, 1);
  public static Command insertWptAfterCommand=new Command("Insert after", Command.OK, 1);
  public static Command hideCommand=new Command("Hide", Command.OK, 1);
  public static Command hardwareCommand=new Command("Hardware", Command.OK, 1);
  // JSR-75
  public static Command loadCommand=new Command("Load >", Command.OK, 1);
  public static Command saveCommand=new Command("Save >", Command.OK, 1);
  public static Command saveFileCommand=new Command("Save file", Command.OK, 1);
  public static Command loadCommand2=new Command("Load >", Command.OK, 1);
  public static Command lockCommand=new Command("Lock/unlock keyboard", Command.OK, 1);
  public static Command codesCommand=new Command("Help", Command.OK, 1);
}
