import UIKit
import Flutter
import Braintree

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    
    override func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
      GeneratedPluginRegistrant.register(with: self)
      BTAppSwitch.setReturnURLScheme("in.appyflow.flutterBraintreeExample")
      
      return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    
    
    override func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        if url.scheme == "in.appyflow.flutterBraintreeExample" {
            return BTAppSwitch.handleOpen(url, options:options)
        }
        
        return false
    }

}
