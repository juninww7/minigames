# FR32Minigames

Base funcional do plugin `FR32Minigames` para Spigot/Paper com foco em:

- Core de arenas e estados globais.
- Auto start por mínimo de jogadores com cancelamento automático da contagem.
- Minigame Batata Quente com transferência da batata por hit.
- Comandos em português com TabCompleter.
- Permissões separadas (`batata.play` e `batata.admin`).
- Scoreboard sidebar padrão roxo/preto atualizada em tempo real.
- Persistência de vitórias por jogador em `stats.yml`.

## Comandos

### Global
- `/minigame entrar <arena>`
- `/minigame sair`
- `/minigame info`
- `/minigame status`

### Batata Quente (player)
- `/batata entrar <arena>`
- `/batata sair`
- `/batata info`

### Batata Quente (admin)
- `/batata admin criar <arena>`
- `/batata admin setspawn <arena>`
- `/batata admin start <arena>`
- `/batata admin stop <arena>`
- `/batata admin reload`

## Build

```bash
mvn -q -DskipTests package
```

## Próximos passos

- Implementar minigame Chão é Lava sobre a mesma API interna.
- Adicionar ranking global detalhado por minigame.
- Expandir sistema de lobby com `/minigame setlobby` e fallback no quit.
