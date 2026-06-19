import SwiftUI
import shared

struct ContentView: View {
    @State private var result: String = ""
    @State private var isLoading: Bool = false

    private let apiClient: HealthApiClient

    init() {
        let httpClient = HttpClientFactoryKt.createHttpClient()
        apiClient = HealthApiClient(httpClient: httpClient)
    }

    var body: some View {
        VStack(spacing: 20) {
            Text("Designer KMP Template")
                .font(.largeTitle)

            Text("macOS Native App")
                .foregroundColor(.secondary)

            Text("Platform: \(Platform_macosKt.getPlatform().name)")
                .font(.caption)
                .foregroundColor(.secondary)

            Button("API を呼び出す") {
                callApi()
            }
            .disabled(isLoading)

            if isLoading {
                ProgressView()
                    .controlSize(.small)
            }

            if !result.isEmpty {
                Text(result)
                    .textSelection(.enabled)
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(8)
            }
        }
        .padding(40)
        .frame(minWidth: 400, minHeight: 300)
    }

    private func callApi() {
        isLoading = true
        result = ""
        apiClient.checkHealth { response, error in
            DispatchQueue.main.async {
                isLoading = false
                if let response = response {
                    result = "IP: \(response.origin)"
                } else if let error = error {
                    result = "Error: \(error.localizedDescription)"
                }
            }
        }
    }
}

#Preview {
    ContentView()
}
