import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import junkdrawer.composeapp.generated.resources.Res
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class NameAndDescription(
    val name: String,
    val description: String,
)

