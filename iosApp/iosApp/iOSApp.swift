import SwiftUI
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        setupLogging()
        return true
    }
    
    private func setupLogging() {
        Cedar.Forest.shared.i(message: "Cedar Logger initialized from Swift!", throwable: nil)
    }

}

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
