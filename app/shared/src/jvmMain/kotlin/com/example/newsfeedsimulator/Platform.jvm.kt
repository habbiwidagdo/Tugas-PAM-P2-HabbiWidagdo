package com.example.newsfeedsimulator

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

data class News(
    val id: Int,
    val title: String,
    val category: String,
    val content: String
)

data class NewsUiModel(
    val displayTitle: String,
    val categoryLabel: String
)

class NewsRepository {

    private val newsList = listOf(
        News(
            id = 1,
            title = "Kotlin 2.0 Resmi Dirilis",
            category = "Technology",
            content = "Kotlin 2.0 membawa peningkatan compiler dan performa."
        ),
        News(
            id = 2,
            title = "Tim Nasional Menang dalam Pertandingan",
            category = "Sports",
            content = "Tim nasional berhasil memenangkan pertandingan dengan skor 3-1."
        ),
        News(
            id = 3,
            title = "Perkembangan AI Semakin Pesat",
            category = "Technology",
            content = "Teknologi Artificial Intelligence berkembang dengan sangat cepat."
        ),
        News(
            id = 4,
            title = "Festival Musik Digelar Akhir Pekan",
            category = "Entertainment",
            content = "Festival musik akan menghadirkan berbagai musisi populer."
        ),
        News(
            id = 5,
            title = "Smartphone Baru Resmi Diluncurkan",
            category = "Technology",
            content = "Smartphone generasi terbaru menawarkan performa yang lebih tinggi."
        ),
        News(
            id = 6,
            title = "Liga Sepak Bola Memasuki Babak Final",
            category = "Sports",
            content = "Babak final liga sepak bola akan berlangsung akhir pekan ini."
        ),
        News(
            id = 7,
            title = "Film Baru Mendapat Sambutan Positif",
            category = "Entertainment",
            content = "Film terbaru mendapatkan banyak ulasan positif dari penonton."
        ),
        News(
            id = 8,
            title = "Perkembangan Teknologi Cloud Computing",
            category = "Technology",
            content = "Cloud computing semakin banyak digunakan oleh perusahaan."
        )
    )

    // Coroutine untuk mengambil detail berita secara async
    suspend fun getNewsDetail(id: Int): News? {
        delay(1000) // Simulasi network delay
        return newsList.find { it.id == id }
    }

    // Flow yang mensimulasikan data berita baru setiap 2 detik
    fun newsFlow(): Flow<News> = flow {
        for (news in newsList) {
            emit(news)
            delay(2000)
        }
    }
}

class NewsViewModel(private val newsRepository: NewsRepository) {
    // StateFlow untuk menyimpan jumlah berita yang sudah dibaca
    private val _readCount = MutableStateFlow(0)
    val readCount: StateFlow<Int> = _readCount.asStateFlow()

    fun markNewsAsRead() {
        _readCount.value++
    }

    // Filter berita berdasarkan kategori tertentu dan transform data menjadi format yang ditampilkan
    fun getFilteredAndTransformedNewsFlow(targetCategory: String): Flow<NewsUiModel> {
        return newsRepository.newsFlow()
            .filter { it.category == targetCategory }
            .map { news ->
                NewsUiModel(
                    displayTitle = "[BREAKING] ${news.title}",
                    categoryLabel = news.category.uppercase()
                )
            }
    }

    // Coroutine untuk mengambil detail berita secara async melalui repository
    suspend fun fetchNewsDetail(id: Int): News? {
        return newsRepository.getNewsDetail(id)
    }
}

fun main() = runBlocking {
    val repository = NewsRepository()
    val viewModel = NewsViewModel(repository)

    println("=== Simulasi NewsFeedSimulator ===")

    // 1. Observe StateFlow jumlah berita yang sudah dibaca
    val readCountJob = launch {
        viewModel.readCount.collect { count ->
            println("[StateFlow] Jumlah berita dibaca: $count")
        }
    }

    // 2 & 3. Flow simulasi berita baru setiap 2 detik, filter kategori "Technology", dan transform data
    val targetCategory = "Technology"
    println("Menerima berita untuk kategori: $targetCategory")

    val collectJob = launch {
        viewModel.getFilteredAndTransformedNewsFlow(targetCategory).collect { uiModel ->
            println("[Flow] Diterima -> Kategori: ${uiModel.categoryLabel} | Judul: ${uiModel.displayTitle}")
            
            // Simulasi membaca berita (menaikkan StateFlow count)
            viewModel.markNewsAsRead()

            // 5. Coroutines untuk mengambil detail berita secara async
            val detail = viewModel.fetchNewsDetail(1)
            if (detail != null) {
                println("  [Async Detail] Konten berita ID ${detail.id}: ${detail.content}")
            }
        }
    }

    // Biarkan berjalan beberapa detik untuk simulasi
    delay(10000)

    collectJob.cancel()
    readCountJob.cancel()
    println("=== Simulasi Selesai ===")
}
