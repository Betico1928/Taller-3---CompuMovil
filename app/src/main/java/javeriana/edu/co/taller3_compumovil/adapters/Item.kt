package javeriana.edu.co.taller3_compumovil.adapters

import android.net.Uri
import android.view.View

data class Item(val imageResource: Uri, val text: String, val buttonClickListener: View.OnClickListener)