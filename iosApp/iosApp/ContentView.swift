import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Güncelleme gerekirse buraya eklenebilir
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
