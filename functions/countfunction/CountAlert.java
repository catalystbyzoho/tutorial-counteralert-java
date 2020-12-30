
import com.catalyst.Context;
import com.catalyst.basic.BasicIO;
import com.catalyst.basic.ZCFunction;
import com.zc.component.cache.ZCCache;
import com.zc.component.mail.ZCMail;
import com.zc.component.mail.ZCMailContent;

import java.util.logging.Logger;
import java.util.logging.Level;

public class CountAlert implements ZCFunction {
 private static final Logger LOGGER = Logger.getLogger(CountAlert.class.getName());

 private static final String FROM_EMAIL="srinath.pasupathi@gmail.com"; // The email address of the sender. Replace this with the email address you configured in Mail.

 private static final String TO_EMAIL="srinath.pasupathi@gmail.com"; // Replace this with the email address that you want the alert email to be sent to. 

 @Override
    public void runner(Context context, BasicIO basicIO) throws Exception {

  try {
   // Passes the feature name that was obtained from the input as the parameter. The feature name can be 'CRM' or 'Desk'.
   String featureName = (String) basicIO.getParameter("feature_name");

   // Passes the threshold count that was obtained from the input as the parameter
   String mailCountThreshold = (String) basicIO.getParameter("mail_count_threshold");

   // Gets the current count for the feature from the default segment of Catalyst Cache
   String value = ZCCache.getInstance()
          .getCacheValue("COUNTER_" + featureName)
          .getValue();

   // If the value is null, then the count is set to 1. This is done during the first execution for the feature.
   if (value == null) {
    ZCCache.getInstance()
        .putCacheValue("COUNTER_" + featureName, String.valueOf(1), 1l);
   } else {
    Integer count = Integer.valueOf(value);

    // If the value is not null, then the count is incremented by 1.
    count++;

    // Writes the count value to the default segment of Catalyst cache
    ZCCache.getInstance().putCacheValue("COUNTER_"+featureName,count.toString(),1l);

    // If the current count is greater than the threshold value, then an email alert is sent to the receiver configured above
    if (count > Integer.valueOf(mailCountThreshold)) {
     ZCMailContent content = ZCMailContent.getInstance();
     content.setFromEmail(FROM_EMAIL);
     content.setSubject("Alert!");
     content.setContent("Count exceeded the threshold limit for the feature:" +featureName);
     content.setToEmail(TO_EMAIL);
     ZCMail.getInstance()
        .sendMail(content);
     LOGGER.log(Level.INFO, "Email alert sent");//Written to the logs. You can view this log from Logs under the Monitor section in the console
    }
   }

   // Handles the 200 status code
   basicIO.setStatus(200);
   basicIO.write("The function executed successfully");
  }catch(Exception e)
  {
   LOGGER.log(Level.SEVERE,"Exception occurred while processing",e); //Written to the logs. You can view this log from Logs under the Monitor section in the console

   // Handles the 500 status code, when an exception occurs
   basicIO.setStatus(500);
   basicIO.write("Error in the function execution");
  }
 }
}
