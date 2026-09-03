import UIKit
import ComposeApp

class ViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        let vc = MainViewControllerKt.MainViewController()
        addChild(vc)
        view.addSubview(vc.view)
        vc.didMove(toParent: self)
    }
}
