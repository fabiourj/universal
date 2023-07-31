#!/bin/bash

pacote=com.mundo.curioso

if [  -z $1    ]; then
   echo "entre com um nome do pacote"
   exit
elif [ $1 = "$pacote"  ]; then
echo "ERRO! Nome do PACOTE igual do aplicativo."
else
#aplicar

sed -i  -e "s:$pacote:$1:g"  app/google-services.json
sed -i  -e "s:$pacote:$1:g"  app/src/main/AndroidManifest.xml
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/listeners/OnHomePageItemClickListener.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/listeners/onPostClickListener.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/listeners/OnItemClickListener.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/listeners/OnCommentClickListener.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/PublishPostActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/ImageViewerActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/DetailActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/BottomSheetFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/model/MenuDrawer.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/model/menuItems.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/model/BottomNavigationMenu.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/model/ComplexHome.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/model/WorDroidObject.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/model/Settings.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/Config.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/ItemOffsetDecoration.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/EqualSpacingItemDecoration.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/Utils.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/ImageFileProvider.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/CategoryDiffUtilCallback.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/SpaceItemDecoration.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/WebAppInterface.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/views/CustomViewPager.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/DiffUtilCallback.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/EndlessScrollListener.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/HackyViewPager.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/CustomCollapsingToolbarLayout.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/Settings.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/others/Constants.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/PostContainerActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/WordroidTestActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/media/MediaFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/media/MediaViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/media/MediaAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/user/AuthorFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/user/AuthorAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/user/AuthorViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/settings/SettingsFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/postlist/PostListViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/postlist/PostListFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/auth/AuthFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/category/CategoryFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/category/CategoryAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/category/CategoryViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/ViewPagerTabFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/tags/TagsViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/tags/TagsFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/tags/TagsAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/DemoFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/saved/SavedPostAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/saved/SavedPostViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/saved/SavedPostFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/comments/CommentsViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/comments/ReplyCommentDialogFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/comments/CommentsFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/comments/CommentAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/ImageFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/TabPagerAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/WPAuthFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/DemoViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/homepage/HomePageFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/homepage/HomePageViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/post/PostFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/post/PostViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/webview/WebViewFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/relatedpost/RelatedPostFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/relatedpost/RelatedPostViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/relatedpost/RelatedPostAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/notification/NotificationViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/notification/NotificationAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/notification/NotificationFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/youtube/PlaylistViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/youtube/VideoAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/youtube/VideoFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/youtube/VideoViewModel.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/youtube/PlaylistAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/fragment/youtube/PlaylistFragment.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/AdHelper.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/BlockTypes.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/PostParametersActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/adapter/ImageViewerViewPagerAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/adapter/ViewPagerAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/adapter/ParentAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/adapter/ComplexRecyclerViewAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/adapter/PostListAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/adapter/ChildAdapter.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/SplashActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/ContainerActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/viewholders/UnifiedNativeAdViewHolder.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/viewholders/LoadingViewHolder.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/viewholders/EmptySpaceViewHolder.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/viewholders/FBNativeAdViewHolder.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/viewholders/BigPictureViewHolder.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/AuthActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/MainActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/IntroActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/YoutubePlayerActivity.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/java/com/arena/esportes/MainApplication.java
sed -i  -e "s:$pacote:$1:g"  app/src/main/res/layout/activity_image_viewer.xml
#sed -i  -e "s:$pacote:$1:g"  app/src/androidTest/java/com/arena/esportes/ExampleInstrumentedTest.java
#sed -i  -e "s:$pacote:$1:g"  app/src/test/java/com/arena/esportes/ExampleUnitTest.java
sed -i  -e "s:$pacote:$1:g"  app/build.gradle

 
##################################################################################333###############################
#::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
rm -rf .gradle/
#:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
#::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

echo "PACOTE MODIFICADO COM SUCESSO!!!"
fi
