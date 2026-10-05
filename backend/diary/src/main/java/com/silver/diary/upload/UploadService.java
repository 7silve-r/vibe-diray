package com.silver.diary.upload;

import com.silver.diary.entity.Article;
import com.silver.diary.entity.User;
import com.silver.diary.exception.BusinessException;
import com.silver.diary.service.ArticleService;
import com.silver.diary.service.UserService;
import java.io.IOException;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {
    private final LocalFileStorage localFileStorage;
    private final UserService userService;
    private final ArticleService articleService;
    private final TransactionTemplate transactions;

    public UploadService(
            LocalFileStorage localFileStorage,
            UserService userService,
            ArticleService articleService,
            PlatformTransactionManager manager) {
        this.localFileStorage = localFileStorage;
        this.userService = userService;
        this.articleService = articleService;
        this.transactions = new TransactionTemplate(manager);
    }

    public UploadResult avatar(String username, MultipartFile file) throws IOException {
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) throw new BusinessException(401, "账号不存在，请重新登录");
        if (file == null) throw new BusinessException("请选择头像文件");
        return save(
                file,
                "avatars",
                user.getUserPic(),
                url -> {
                    User update = new User();
                    update.setId(user.getId());
                    update.setUserPic(url);
                    if (!userService.updateById(update)) throw new BusinessException("头像更新失败");
                });
    }

    public UploadResult article(Article article, MultipartFile file) throws IOException {
        return save(
                file,
                "covers",
                article.getCoverImg(),
                url -> {
                    article.setCoverImg(url);
                    boolean ok =
                            article.getId() == null
                                    ? articleService.save(article)
                                    : articleService.updateById(article);
                    if (!ok) throw new BusinessException("日记保存失败，请刷新后重试");
                });
    }

    private UploadResult save(
            MultipartFile file, String folder, String oldUrl, Consumer<String> update)
            throws IOException {
        String url = file == null ? oldUrl : localFileStorage.save(file, folder);
        try {
            transactions.executeWithoutResult(status -> update.accept(url));
        } catch (RuntimeException ex) {
            if (file != null) localFileStorage.deleteQuietly(url);
            throw ex;
        }
        if (file != null) localFileStorage.deleteQuietly(oldUrl);
        return new UploadResult(url);
    }
}
