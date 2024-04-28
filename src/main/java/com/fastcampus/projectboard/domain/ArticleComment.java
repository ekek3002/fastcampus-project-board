package com.fastcampus.projectboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@ToString(callSuper = true)
@Table(indexes = {
        @Index(columnList = "content"),
        @Index(columnList = "createdAt"),
        @Index(columnList = "createdBy"),
})
@EntityListeners(AuditingEntityListener.class)
@Entity
public class ArticleComment extends AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(optional = false)
    private Article article; // 게시글 (ID)

    @Setter
    @JoinColumn(name = "userId")
    @ManyToOne(optional = false)
    private UserAccount userAccount;

    @Setter
    @Column(updatable = false) // 자식 댓글의 부모 댓글이 바뀌는 일은 없기 때문에 updatable = false
    private Long parentCommentId; // 부모 댓글 ID

    @ToString.Exclude //스택오버플로우 에러가 나지 않게 설정
    @OrderBy("createdAt ASC") // 자식 댓글은 작성 오름차순으로 정렬
    @OneToMany(mappedBy = "parentCommentId", cascade = CascadeType.ALL) // 부모 댓글이 지워지면 자식 댓글도 삭제 부모와 자식의 관계는 긴밀하므로 cascade = CascadeType.ALL
    private Set<ArticleComment> childComments = new LinkedHashSet<>(); // 하이버네이트 표준 스펙에 의하면 jpa 코드에서는 final 키워드를 쓰지 않을 것을 권고

    @Setter @Column(nullable = false, length = 500) private String content; // 본문


    protected ArticleComment() {}

    private ArticleComment(Article article, UserAccount userAccount, Long parentCommentId, String content) {
        this.article = article;
        this.userAccount = userAccount;
        this.parentCommentId = parentCommentId;
        this.content = content;
    }

    public static ArticleComment of(Article article, UserAccount userAccount, String content) {
        return new ArticleComment(article, userAccount, null, content);
    }

    public void addChildComment(ArticleComment child) { //부모 자식 관계를 이 안에서 셋팅
        child.setParentCommentId(this.getId());
        this.getChildComments().add(child);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleComment that)) return false;
        return this.getId() != null && this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
