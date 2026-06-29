@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class DashboardData(val totalBalance: Double)

val json = Json { namingStrategy = JsonNamingStrategy.SnakeCase }
println(json.encodeToString(DashboardData(10.0)))
