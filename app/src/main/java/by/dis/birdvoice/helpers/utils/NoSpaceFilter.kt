package by.dis.birdvoice.helpers.utils

import android.text.InputFilter
import android.text.Spanned
import android.widget.Toast
import by.dis.birdvoice.R
import by.dis.birdvoice.app.MainApp

class NoSpaceFilter(private val mainApp: MainApp) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        //Space
        for (i in start until end) {
            val c = source?.get(i)
            if (c == ' ') {
                Toast.makeText(mainApp.getContext(), mainApp.getContext().getString(R.string.space_is_not_allowed), Toast.LENGTH_SHORT).show()
                return ""
            }
        }

        return null
    }
}
