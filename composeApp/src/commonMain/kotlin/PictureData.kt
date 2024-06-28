import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class PictureData(
    val name: String,
    val dateString: String,
    val platformStorableImage: PlatformStorableImage
) {
    constructor(
        name: String,
        timeStampSeconds: Long,
        platformStorableImage: PlatformStorableImage
    ) : this(name, getDateString(timeStampSeconds), platformStorableImage)
}

private fun getDateString(timeStampSeconds: Long): String {
    val instantTime = Instant.fromEpochSeconds(timeStampSeconds, 0)
    val utcTime = instantTime.toLocalDateTime(TimeZone.UTC)
    val date = utcTime.date
    val monthStr = date.month.name.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        .take(3)
    val dayStr = date.dayOfMonth
    return "$dayStr $monthStr."
}
