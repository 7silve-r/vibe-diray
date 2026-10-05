# 后端接口清单

统一返回 `{ "code": 200, "message": "...", "data": ... }`；失败通过全局异常处理，HTTP 状态与 code 一致。分页 data 使用 total 和 list。

日记发布和修改使用 multipart/form-data，字段 title、cateId、content、state、file；file 可选。state 必须为“私有”或“公开”。登录和注册请求字段保持原 diary DTO。

邮箱验证码：POST /api/email/code，JSON 为 email 和 purpose（bind 或 reset）；bind 用途需登录。绑定 POST /my/email，JSON 为 email 和 code；重置 POST /api/password/reset，JSON 为 email、code、newPwd、reNewPwd。

下表列出实际映射。音乐业务路径统一以 /music 开头；不再使用原音乐的 /user/login 或 /admin/login。具体 JSON 字段见对应 dto 文件，文件上传见 Controller 的 @RequestParam。

| 模块 / 控制器 | 方法 | 路径 | Java 方法 |
| --- | --- | --- | --- |
| diary/AdminUserController | GET | `/admin/users` | list |
| diary/AdminUserController | POST | `/admin/users` | add |
| diary/AdminUserController | PATCH | `/admin/users/{id}/profile` | update |
| diary/AdminUserController | PATCH | `/admin/users/{id}/status` | status |
| diary/AdminUserController | DELETE | `/admin/users/{id}` | delete |
| diary/ArticleController | GET | `/my/article/list` | list |
| diary/ArticleController | GET | `/my/article/info` | detail |
| diary/ArticleController | POST | `/my/article` | publish |
| diary/ArticleController | PUT | `/my/article/{id}` | update |
| diary/ArticleController | DELETE | `/my/article` | delete |
| diary/ArticleSocialController | GET | `/public/articles` | list |
| diary/ArticleSocialController | GET | `/public/articles/{id}` | detail |
| diary/ArticleSocialController | GET | `/my/article/favorites` | favorites |
| diary/ArticleSocialController | GET | `/public/articles/{id}/comments` | comments |
| diary/ArticleSocialController | POST | `/my/article/{id}/comment` | comment |
| diary/ArticleSocialController | DELETE | `/my/article/comments/{id}` | deleteComment |
| diary/ArticleSocialController | DELETE | `/admin/diary/articles/{id}` | delete |
| diary/ArticleSocialController | POST | `/my/article/{id}/like` | like |
| diary/ArticleSocialController | DELETE | `/my/article/{id}/like` | cancelLike |
| diary/ArticleSocialController | POST | `/my/article/{id}/favorite` | collect |
| diary/ArticleSocialController | DELETE | `/my/article/{id}/favorite` | cancelCollect |
| diary/ArticleSocialController | POST | `/my/article/comments/{id}/like` | likeComment |
| diary/ArticleSocialController | DELETE | `/my/article/comments/{id}/like` | cancelLikeComment |
| diary/CategoryController | GET | `/my/categories` | list |
| diary/CategoryController | POST | `/my/categories` | add |
| diary/CategoryController | PUT | `/my/categories` | update |
| diary/CategoryController | DELETE | `/my/categories` | delete |
| diary/EmailController | POST | `/api/email/code` | send |
| diary/EmailController | POST | `/my/email` | bind |
| diary/EmailController | POST | `/api/password/reset` | reset |
| diary/FileController | GET | `${file.access-url-prefix}{folder}/{name}` | file |
| diary/UserController | POST | `/api/reg` | register |
| diary/UserController | POST | `/api/login` | login |
| diary/UserController | GET | `/my/profile` | getUserInfo |
| diary/UserController | PATCH | `/my/profile` | updateProfile |
| diary/UserController | PATCH | `/my/password` | updatePassword |
| diary/UserController | PATCH | `/my/avatar` | updateAvatar |
| diary/UserController | POST | `/my/logout` | logout |
| diary/UserController | DELETE | `/my/account` | delete |
| music/AdminController | GET | `/music/admin/countArtists` | countArtists |
| music/AdminController | POST | `/music/admin/listArtists` | listArtists |
| music/AdminController | POST | `/music/admin/addArtist` | addArtist |
| music/AdminController | PUT | `/music/admin/updateArtist` | updateArtist |
| music/AdminController | DELETE | `/music/admin/deleteArtist/{id}` | deleteArtist |
| music/AdminController | DELETE | `/music/admin/deleteArtists` | deleteArtists |
| music/AdminController | GET | `/music/admin/countSongs` | countSongs |
| music/AdminController | GET | `/music/admin/listArtistNames` | listArtistNames |
| music/AdminController | POST | `/music/admin/listAdminSongs` | listAdminSongs |
| music/AdminController | POST | `/music/admin/addSong` | addSong |
| music/AdminController | PUT | `/music/admin/updateSong` | updateSong |
| music/AdminController | DELETE | `/music/admin/deleteSong/{id}` | deleteSong |
| music/AdminController | DELETE | `/music/admin/deleteSongs` | deleteSongs |
| music/AdminController | GET | `/music/admin/countPlaylists` | countPlaylists |
| music/AdminController | POST | `/music/admin/listPlaylists` | listPlaylists |
| music/AdminController | POST | `/music/admin/addPlaylist` | addPlaylist |
| music/AdminController | PUT | `/music/admin/updatePlaylist` | updatePlaylist |
| music/AdminController | DELETE | `/music/admin/deletePlaylist/{id}` | deletePlaylist |
| music/AdminController | DELETE | `/music/admin/deletePlaylists` | deletePlaylists |
| music/ArtistController | POST | `/music/public/artist/listArtists` | listArtists |
| music/ArtistController | GET | `/music/public/artist/getRandomArtists` | getRandomArtists |
| music/ArtistController | GET | `/music/public/artist/getArtistDetail/{id}` | getArtistDetail |
| music/BannerController | POST | `/music/admin/listBanners` | listBanners |
| music/BannerController | PATCH | `/music/admin/updateBannerStatus/{id}` | updateBannerStatus |
| music/BannerController | DELETE | `/music/admin/deleteBanner/{id}` | deleteBanner |
| music/BannerController | DELETE | `/music/admin/deleteBanners` | deleteBanners |
| music/BannerController | GET | `/music/public/banner/getBannerList` | getBannerList |
| music/CommentController | POST | `/music/comment/addSongComment` | addSongComment |
| music/CommentController | POST | `/music/comment/addPlaylistComment` | addPlaylistComment |
| music/CommentController | PATCH | `/music/comment/likeComment/{id}` | likeComment |
| music/CommentController | PATCH | `/music/comment/cancelLikeComment/{id}` | cancelLikeComment |
| music/CommentController | DELETE | `/music/comment/deleteComment/{id}` | deleteComment |
| music/FeedbackController | POST | `/music/admin/listFeedback` | listFeedback |
| music/FeedbackController | DELETE | `/music/admin/deleteFeedback/{id}` | deleteFeedback |
| music/FeedbackController | DELETE | `/music/admin/deleteFeedbacks` | deleteFeedbacks |
| music/FeedbackController | POST | `/music/feedback/addFeedback` | addFeedback |
| music/MediaUploadController | PUT | `/music/admin/artists/{id}/avatar` | artistAvatar |
| music/MediaUploadController | PUT | `/music/admin/songs/{id}/cover` | songCover |
| music/MediaUploadController | PUT | `/music/admin/songs/{id}/audio` | songAudio |
| music/MediaUploadController | PUT | `/music/admin/playlists/{id}/cover` | playlistCover |
| music/MediaUploadController | POST | `/music/admin/banners` | addBanner |
| music/MediaUploadController | PUT | `/music/admin/banners/{id}/image` | bannerImage |
| music/PlaylistBindingController | PUT | `/music/admin/playlists/{id}/songs` | update |
| music/PlaylistController | POST | `/music/public/playlist/listPlaylists` | listPlaylists |
| music/PlaylistController | GET | `/music/public/playlist/getRecommendedPlaylists` | getRecommendedPlaylists |
| music/PlaylistController | GET | `/music/public/playlist/getPlaylistDetail/{id}` | getPlaylistDetail |
| music/SongController | POST | `/music/public/song/listSongs` | listSongs |
| music/SongController | GET | `/music/public/song/getRecommendedSongs` | getRecommendedSongs |
| music/SongController | GET | `/music/public/song/getSongDetail/{id}` | getSongDetail |
| music/StyleController | GET | `/music/public/styles` | list |
| music/StyleController | POST | `/music/admin/styles` | add |
| music/UserFavoriteController | POST | `/music/favorite/getFavoriteSongs` | getUserFavoriteSongs |
| music/UserFavoriteController | POST | `/music/favorite/collectSong` | collectSong |
| music/UserFavoriteController | DELETE | `/music/favorite/cancelCollectSong` | cancelCollectSong |
| music/UserFavoriteController | POST | `/music/favorite/getFavoritePlaylists` | getFavoritePlaylists |
| music/UserFavoriteController | POST | `/music/favorite/collectPlaylist` | collectPlaylist |
| music/UserFavoriteController | DELETE | `/music/favorite/cancelCollectPlaylist` | cancelCollectPlaylist |

歌曲和歌单的 `favoriteStatus` 为收藏状态（0 未收藏，1 已收藏）；评论的 `likeCount` 为点赞数。空分页始终返回数组 `list: []`，保留符合筛选条件的 `total`。
