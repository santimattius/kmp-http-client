import SwiftUI
import Shared

struct ContentView: View {
    
    @State private var viewModel = ContenViewModel()
    
    var body: some View {
        VStack {
            VStack(spacing: 16) {
                Image(systemName: "swift")
                    .font(.system(size: 200))
                    .foregroundColor(.accentColor)
                Text("SwiftUI: \(Greeting().greet())")
            }
            Button("Click me!"){
                viewModel.call()
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
