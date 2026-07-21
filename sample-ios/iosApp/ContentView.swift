import SwiftUI
// import VectoraSearch // Assuming this is linked

struct ContentView: View {
    @State private var query = "hi. give me a list of your best nike shoes that are in black color"
    @State private var results: [String] = [] // Simple list for now
    @State private var status = "Ready (Search logic to be linked)"

    let products = [
        "Nike Air Max 270 - Black",
        "Nike Air Force 1 '07 - Black",
        "Nike Zoom Pegasus 40 - Black/Anthracite",
        "Nike Revolution 6 Next Nature - Triple Black",
        "Nike Court Vision Low - Black/Black",
        "Nike Blazer Mid '77 Vintage - Black/White",
        "Nike Air Max Excee - Black/White",
        "Nike Tanjun - Black/White",
        "Nike Downshifter 12 - Black/Dark Smoke Grey",
        "Nike Air Max Dawn - Black/White",
        "Nike Air Max 90 - Black/Black/White",
        "Nike Air Max 97 - Black/White",
        "Nike Air Max Plus - Black/Black",
        "Nike Waffle Debut - Black/White",
        "Nike React Vision - Black/White/Grey",
        "Nike SB Dunk Low Pro - Black/White",
        "Nike Air Huarache - Triple Black",
        "Nike Air Presto - Black/Black",
        "Nike Air VaporMax Plus - Triple Black",
        "Nike Renew Ride 3 - Black/White",
        "Nike Quest 5 - Black/White",
        "Nike Legend Essential 3 Next Nature - Black",
        "Nike SuperRep Go 3 Next Nature Flyknit - Black",
        "Nike MC Trainer 2 - Black/White",
        "Nike Juniper Trail 2 Next Nature - Black",
        "Adidas Ultraboost 22 - Core Black",
        "Puma Suede Classic XXI - Puma Black",
        "Reebok Classic Leather - Black",
        "New Balance 574 - Black/White",
        "Asics Gel-Kayano 29 - Black/White"
    ]

    var body: some View {
        VStack(alignment: .leading) {
            Text("Vectora Search")
                .font(.largeTitle)
                .bold()

            Text(status)
                .font(.caption)
                .foregroundColor(.gray)

            TextField("Search products...", text: $query)
                .textFieldStyle(.roundedBorder)
                .padding(.vertical)

            Button(action: {
                performSearch()
            }) {
                Text("Search")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }

            Text("Results:")
                .font(.headline)
                .padding(.top)

            List(results, id: \.self) { result in
                Text(result)
            }
        }
        .padding()
    }

    func performSearch() {
        // Mock search for now until KMP framework is fully integrated in Xcode
        status = "Searching for: \(query)"
        results = products.filter { $0.lowercased().contains("black") && $0.lowercased().contains("nike") }.prefix(10).map { $0 }
    }
}
