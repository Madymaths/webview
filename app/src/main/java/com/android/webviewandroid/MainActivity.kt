package com.android.webviewandroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.webviewandroid.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val webViewUrl: String = "https://www.google.com"
    private lateinit var binding: ActivityMainBinding
    private var upload: ValueCallback<Array<Uri>>? = null
    var backPressedCount = 0
    private val handler = Handler(Looper.getMainLooper())

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                if (upload != null) {
                    upload?.onReceiveValue(
                        WebChromeClient.FileChooserParams.parseResult(
                            result.resultCode,
                            result.data
                        )
                    )
                    upload = null
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadWebView()
    }

    private fun loadWebView() {
        if (Uri.parse(webViewUrl).isAbsolute) {
            binding.also {
                it.webView.settings.javaScriptEnabled = true
                it.webView.settings.setSupportZoom(false)
                it.webView.settings.loadWithOverviewMode = true
                it.webView.settings.javaScriptCanOpenWindowsAutomatically = true
                it.webView.settings.setSupportMultipleWindows(true)
                it.webView.settings.domStorageEnabled = true
                it.webView.settings.allowFileAccess = true
                it.progressBar.visibility = View.VISIBLE
                it.webView.clearHistory()
                it.webView.clearCache(true)
                it.webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        it.progressBar.visibility = View.GONE
                    }

                    override fun onPageCommitVisible(view: WebView?, url: String?) {
                        super.onPageCommitVisible(view, url)
                        it.progressBar.visibility = View.GONE
                    }

                    override fun onReceivedError(
                        view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        it.progressBar.visibility = View.GONE
                    }
                }
                it.webView.setDownloadListener { url, _, _, _, _ ->
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
                it.webView.loadUrl(webViewUrl)
                it.webView.webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest) {
                        request.grant(request.resources)
                    }

                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
                        val intent = fileChooserParams?.createIntent()
                        upload = filePathCallback
                        if (intent != null) {
                            filePicker.launch(intent)
                        }
                        return true
                    }
                }
            }

        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        } else {
            return if (backPressedCount == 1) {
                return super.onKeyDown(keyCode, event)
            } else {
                showToast(this, "Press back again to exit")
                backPressedCount++

                handler.postDelayed({
                    backPressedCount = 0
                }, 2000)
                true
            }
        }
    }

    private fun showToast(activity: Context, toastString: String) {
        try {
            val toast = Toast.makeText(activity, toastString, Toast.LENGTH_SHORT)
            toast.show()
        } catch (e: Exception) {
            //Error
        }
    }
}