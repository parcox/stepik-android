package org.stepik.android.view.base.ui.widget.block

class HighlightScriptBlock : ContentBlock {
    override val header: String = """
        <script type="text/javascript" src="file:///android_asset/scripts/highlight.pack.js"></script>
        <script>hljs.initHighlightingOnLoad();</script>
    """.trimIndent()
    
    override fun isEnabled(content: String): Boolean =
        "<code" in content
}