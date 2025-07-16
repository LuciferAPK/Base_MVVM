package com.example.basemvvm.ui

object NavigationManager {
//    fun navigateToSplash(manager: FragmentManager) {
//        try {
//            val fragment = SplashFragment()
//            manager.beginTransaction().apply {
//                replace(R.id.frameMain, fragment)
//                commitAllowingStateLoss()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

//    fun navigateToMain(manager: FragmentManager) {
//        try {
//            val fragment = MainFragment().apply {
//                arguments = Bundle().apply {}
//            }
//            manager.beginTransaction().apply {
//                setCustomAnimations(
////                    R.anim.slide_in_up,
////                    R.anim.slide_out_down,
////                    R.anim.slide_in_up,
////                    R.anim.slide_out_down
//                    R.anim.new_fade_in,
//                    R.anim.new_fade_out,
//                    R.anim.new_fade_in,
//                    R.anim.new_fade_out
//                )
//                replace(R.id.frame_main, fragment)
//                commitAllowingStateLoss()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

//    fun navigationToLanguage(manager: FragmentManager) {
//        try {
//            val fragment = LanguageFragment()
//            manager.beginTransaction().apply {
//                setCustomAnimations(
//                    R.anim.new_fade_in,
//                    R.anim.new_fade_out,
//                    R.anim.new_fade_in,
//                    R.anim.new_fade_out
//                )
//                replace(R.id.frame_main, fragment)
//                commitAllowingStateLoss()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

//    fun navigatePolicy(activity: Activity) {
//        postDelayedSkipException {
//            val intent = Intent(activity, PolicyActivity::class.java).apply {
//            }
//            activity.startActivity(intent)
//        }
//    }

//    fun navigateToIntro(manager: FragmentManager) {
//        try {
//            val fragment = IntroFragment()
//            manager.beginTransaction().apply {
//                setCustomAnimations(
//                    R.anim.new_fade_in,
//                    R.anim.new_fade_out,
//                    R.anim.new_fade_in,
//                    R.anim.new_fade_out
//                )
//                replace(R.id.frameMain, fragment)
//                commitAllowingStateLoss()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
}