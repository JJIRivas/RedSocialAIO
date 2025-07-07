package com.example.redsocialaio.misskey.conversion;

import com.example.redsocialaio.core.repositories.BaseSocialAccountRepository;
import com.example.redsocialaio.misskey.core.MisskeyAccount;

import java.util.Map;

public class MisskeyAccountRepository extends BaseSocialAccountRepository<MisskeyAccount> {

    @Override
    protected String getDocumentName() {
        return "misskey";
    }

    @Override
    protected Map<String, Object> toFirestoreMap(MisskeyAccount account) {
        return MisskeyAccountMapper.toFirestoreMap(account);
    }

    @Override
    protected MisskeyAccount fromFirestoreMap(Map<String, Object> data) {
        return MisskeyAccountMapper.fromFirestoreMap(data);
    }
}