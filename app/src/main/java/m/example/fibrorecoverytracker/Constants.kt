package m.example.fibrorecoverytracker

import java.time.format.DateTimeFormatter

class Constants {
    companion object {
        val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyy")
        val DATE_FORMATTER2 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}