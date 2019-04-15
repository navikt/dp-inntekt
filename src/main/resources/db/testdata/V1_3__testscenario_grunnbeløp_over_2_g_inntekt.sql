INSERT INTO inntekt_v1(id, inntekt)
values ('01D8G6FS9QGRT3JKBTA5KEE64C', '
{
  "arbeidsInntektMaaned": [
    {
      "aarMaaned": "2019-01",
      "arbeidsInntektInformasjon": {
        "inntektListe": [
          {
            "inntektType": "LOENNSINNTEKT",
            "beloep": 250000,
            "fordel": "kontantytelse",
            "inntektskilde": "A-ordningen",
            "inntektsperiodetype": "Maaned",
            "inntektsstatus": "LoependeInnrapportert",
            "leveringstidspunkt": "2019-02",
            "utbetaltIMaaned": "2018-03",
            "opplysningspliktig": {
              "identifikator": "1111111",
              "aktoerType": "ORGANISASJON"
            },
            "virksomhet": {
              "identifikator": "1111111",
              "aktoerType": "ORGANISASJON"
            },
            "inntektsmottaker": {
              "identifikator": "99999999999",
              "aktoerType": "NATURLIG_IDENT"
            },
            "inngaarIGrunnlagForTrekk": true,
            "utloeserArbeidsgiveravgift": true,
            "informasjonsstatus": "InngaarAlltid",
            "beskrivelse": "fastloenn"
          }
        ]
      }
    },
    {
      "aarMaaned": "2018-03",
      "arbeidsInntektInformasjon": {
        "inntektListe": [
          {
            "inntektType": "LOENNSINNTEKT",
            "beloep": 250000,
            "fordel": "kontantytelse",
            "inntektskilde": "A-ordningen",
            "inntektsperiodetype": "Maaned",
            "inntektsstatus": "LoependeInnrapportert",
            "leveringstidspunkt": "2019-02",
            "utbetaltIMaaned": "2018-03",
            "opplysningspliktig": {
              "identifikator": "1111111",
              "aktoerType": "ORGANISASJON"
            },
            "virksomhet": {
              "identifikator": "1111111",
              "aktoerType": "ORGANISASJON"
            },
            "inntektsmottaker": {
              "identifikator": "99999999999",
              "aktoerType": "NATURLIG_IDENT"
            },
            "inngaarIGrunnlagForTrekk": true,
            "utloeserArbeidsgiveravgift": true,
            "informasjonsstatus": "InngaarAlltid",
            "beskrivelse": "fastloenn"
          }
        ]
      }
    },
    {
      "aarMaaned": "2017-04",
      "arbeidsInntektInformasjon": {
        "inntektListe": [
          {
            "inntektType": "LOENNSINNTEKT",
            "beloep": 250000,
            "fordel": "kontantytelse",
            "inntektskilde": "A-ordningen",
            "inntektsperiodetype": "Maaned",
            "inntektsstatus": "LoependeInnrapportert",
            "leveringstidspunkt": "2019-02",
            "utbetaltIMaaned": "2018-03",
            "opplysningspliktig": {
              "identifikator": "1111111",
              "aktoerType": "ORGANISASJON"
            },
            "virksomhet": {
              "identifikator": "1111111",
              "aktoerType": "ORGANISASJON"
            },
            "inntektsmottaker": {
              "identifikator": "99999999999",
              "aktoerType": "NATURLIG_IDENT"
            },
            "inngaarIGrunnlagForTrekk": true,
            "utloeserArbeidsgiveravgift": true,
            "informasjonsstatus": "InngaarAlltid",
            "beskrivelse": "fastloenn"
          }
        ]
      }
    },
    {
      "aarMaaned": "2017-12",
      "arbeidsInntektInformasjon": {
        "inntektListe": [
          {
            "inntektType": "LOENNSINNTEKT",
            "beloep": 250000,
            "fordel": "kontantytelse",
            "inntektskilde": "A-ordningen",
            "inntektsperiodetype": "Maaned",
            "inntektsstatus": "LoependeInnrapportert",
            "leveringstidspunkt": "2019-02",
            "utbetaltIMaaned": "2018-03",
            "opplysningspliktig": {
              "identifikator": "1111111",
              "aktoerType": "ORGANISASJON"
            },
            "virksomhet": {
              "identifikator": "1111111",
              "aktoerType": "ORGANISASJON"
            },
            "inntektsmottaker": {
              "identifikator": "99999999999",
              "aktoerType": "NATURLIG_IDENT"
            },
            "inngaarIGrunnlagForTrekk": true,
            "utloeserArbeidsgiveravgift": true,
            "informasjonsstatus": "InngaarAlltid",
            "beskrivelse": "fastloenn"
          }
        ]
      }
    }
  ],
  "ident": {
    "identifikator": "-1",
    "aktoerType": "NATURLIG_IDENT"
  }
}')
ON CONFLICT DO NOTHING;

INSERT INTO inntekt_v1_arena_mapping (inntektid, aktørid, vedtakid, beregningsdato)
VALUES ('01D8G6FS9QGRT3JKBTA5KEE64C', '3_G_INNTEKT', 12345, '2019-07-01')
ON CONFLICT DO NOTHING;;
