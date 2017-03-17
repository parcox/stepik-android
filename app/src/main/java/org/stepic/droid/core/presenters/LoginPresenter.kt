package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.model.AuthData
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.social.SocialManager
import org.stepic.droid.web.Api
import org.stepic.droid.web.AuthenticationStepicResponse
import retrofit2.Call
import java.util.concurrent.ThreadPoolExecutor

class LoginPresenter(private val api: Api,
                     private val analytic: Analytic,
                     private val sharedPreferenceHelper: SharedPreferenceHelper,
                     private val threadPoolExecutor: ThreadPoolExecutor,
                     private val mainHandler: MainHandler) : PresenterBase<LoginView>() {

    fun login(rawLogin: String, rawPassword: String) {
        val login = rawLogin.trim()
        doRequest(api.authWithLoginPassword(login, rawPassword), AuthData(login, rawPassword), Type.loginPassword)
    }

    fun loginWithCode(rawCode: String) {
        val code = rawCode.trim()
        doRequest(api.authWithCode(code), null, Type.social)
    }

    fun loginWithNativeProviderCode(nativeCode: String, type: SocialManager.SocialType) {
        val code = nativeCode.trim()
        doRequest(api.authWithNativeCode(code, type),
                null,
                Type.social)
    }

    @MainThread
    private fun doRequest(callToServer: Call<AuthenticationStepicResponse>, authData: AuthData?, type: Type) {
        fun onFail(type: LoginFailType) {
            analytic.reportEventWithName(Analytic.Login.FAIL_LOGIN, type.toString())
            mainHandler.post {
                view?.onFailLogin(type)
            }
        }

        view?.onLoadingWhileLogin()
        threadPoolExecutor.execute {
            try {
                val response = callToServer.execute()
                if (response.isSuccessful) {
                    val authStepikResponse = response.body()
                    if (authStepikResponse != null) {
                        sharedPreferenceHelper.storeAuthInfo(authStepikResponse)
                        analytic.reportEvent(Analytic.Interaction.SUCCESS_LOGIN)

                        mainHandler.post { view?.onSuccessLogin(authData) }
                    } else {
                        analytic.reportEvent(Analytic.Error.UNPREDICTABLE_LOGIN_RESULT)
                        //successful result, but body is not correct
                        onFail(LoginFailType.connectionProblem)
                    }
                } else if (response.code() == 429) {
                    onFail(LoginFailType.tooManyAttempts)
                } else if (response.code() == 401 && type == Type.social) {
                    onFail(LoginFailType.emailAlreadyUsed)
                } else {
                    onFail(LoginFailType.emailPasswordInvalid)
                }
            } catch (ex: Exception) {
                onFail(LoginFailType.connectionProblem)
            }
        }
    }

    private enum class Type {
        social, loginPassword
    }

}
